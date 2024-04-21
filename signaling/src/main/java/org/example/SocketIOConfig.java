package org.example;

import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.listener.DataListener;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.TextMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@org.springframework.context.annotation.Configuration
public class SocketIOConfig {

    @Autowired
    private KurentoClient kurentoClient;


    private final Map<String, WebRtcEndpoint> userToWebRtcEndpoint = new ConcurrentHashMap<>();
    private final Map<String, MediaPipeline> userToMediaPipeline = new ConcurrentHashMap<>();

    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(9898);



        SocketIOServer server = new SocketIOServer(config);

        server.addEventListener("create", JsonObject.class, new DataListener<JsonObject>() {
            @Override
            public void onData(SocketIOClient client, JsonObject data, AckRequest ackRequest) {
                handleCreate(client, String.valueOf(data));
            }
        });

        server.addEventListener("join", String.class, new DataListener<String>() {
            @Override
            public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
                handleJoin(client, data);
            }
        });

        server.addEventListener("leave", String.class, new DataListener<String>() {
            @Override
            public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
                handleLeave(client, data);
            }
        });

        return server;
    }



    private void handleCreate(SocketIOClient client, String data) {
        JsonObject jsonMessage = new JsonParser().parse(data).getAsJsonObject();
        String sdpOffer = jsonMessage.get("sdpOffer").getAsString();

        MediaPipeline pipeline = kurentoClient.createMediaPipeline();
        WebRtcEndpoint webRtcEndpoint = new WebRtcEndpoint.Builder(pipeline).build();
        userToMediaPipeline.put(client.getSessionId().toString(), pipeline);
        userToWebRtcEndpoint.put(client.getSessionId().toString(), webRtcEndpoint);

        String sdpAnswer = webRtcEndpoint.processOffer(sdpOffer);

        JsonObject response = new JsonObject();
        response.addProperty("type", "createResponse");
        response.addProperty("sdpAnswer", sdpAnswer);

        client.sendEvent("createResponse", response.toString());
    }

    private void handleJoin(SocketIOClient client, String data) {
        JsonObject jsonMessage = new JsonParser().parse(data).getAsJsonObject();
        String roomId = jsonMessage.get("roomID").getAsString();

        client.joinRoom(roomId);

        WebRtcEndpoint existingWebRtcEndpoint = userToWebRtcEndpoint.get(roomId);
        if (existingWebRtcEndpoint != null) {
            WebRtcEndpoint newWebRtcEndpoint = new WebRtcEndpoint.Builder(existingWebRtcEndpoint.getMediaPipeline()).build();
            userToWebRtcEndpoint.put(client.getSessionId().toString(), newWebRtcEndpoint);

            // Присоединение к комнате
            existingWebRtcEndpoint.connect(newWebRtcEndpoint);

            // Обработка SDP-оффера и отправка SDP-ответа
            String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
            String sdpAnswer = newWebRtcEndpoint.processOffer(sdpOffer);

            JsonObject response = new JsonObject();
            response.addProperty("type", "joinResponse");
            response.addProperty("sdpAnswer", sdpAnswer);

            client.sendEvent("joinResponse", response.toString());
        } else {
            client.sendEvent("Такая комната уже есть");
        }
    }

    private void handleLeave(SocketIOClient client, String data) {
        JsonObject jsonMessage = new JsonParser().parse(data).getAsJsonObject();
        String roomId = jsonMessage.get("roomID").getAsString();

        // Отключение от комнаты
        client.leaveRoom(roomId);

        // Получение WebRtcEndpoint для комнаты
        WebRtcEndpoint webRtcEndpoint = userToWebRtcEndpoint.get(roomId);
        if (webRtcEndpoint != null) {
            // Освобождение ресурсов WebRtcEndpoint
            webRtcEndpoint.release();
            userToWebRtcEndpoint.remove(roomId);

            // Получение MediaPipeline для комнаты
            MediaPipeline pipeline = userToMediaPipeline.get(roomId);
            if (pipeline != null) {
                // Освобождение ресурсов MediaPipeline
                pipeline.release();
                userToMediaPipeline.remove(roomId);
            }
        }
    }

}
