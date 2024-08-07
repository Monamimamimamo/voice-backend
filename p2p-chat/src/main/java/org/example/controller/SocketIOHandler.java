package org.example.controller;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.Message;
import org.example.domain.MessageRepo;
import org.example.domain.MessageWithRoom;
import org.example.domain.SingleChatJoinData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;

import java.util.*;

@Component
@Slf4j
public class SocketIOHandler {


    @Autowired
    private MessageRepo messageRepo;
    @Getter
    private SocketIONamespace namespace;


    @Autowired
    public SocketIOHandler(SocketIOServer server) {
        this.namespace = server.addNamespace("/chat");

        this.namespace.addEventListener("joinChat", SingleChatJoinData.class, onUserJoinChat);
        this.namespace.addEventListener("sendMessage", MessageWithRoom.class, onUserSendMessage);
        this.namespace.addEventListener("userTyping", String.class, onUserTyping);
    }

    public DataListener<SingleChatJoinData> onUserJoinChat = new DataListener<>() {
        @Override
        public void onData(SocketIOClient client, SingleChatJoinData message, AckRequest arg2) {
            log.info(message.toString());
            String roomID = generateRoomId(message.getSender(), message.getReceiver());
            client.joinRoom(roomID);
            Map<String, String> response = new HashMap<>();
            response.put("sender", message.getSender());
            response.put("roomID", roomID);
            namespace.getBroadcastOperations().sendEvent("joinResponse", response);
        }
    };

    public DataListener<MessageWithRoom> onUserSendMessage = new DataListener<>() {
        @Override
        public void onData(SocketIOClient client, MessageWithRoom message, AckRequest arg2) {
            String roomID = message.getRoomID();
            Message message1 = message.getMessage();
            if (client.getAllRooms().contains(roomID)) {
                namespace.getRoomOperations(roomID).sendEvent("newMessage", message1.getContent(), message1.getSender());
            } else {
                log.warn("Клиент не находится в ожидаемой комнате: {}", roomID);
            }

            log.info(message.toString());
            messageRepo.save(message.getMessage());
        }
    };



    public DataListener<String> onUserTyping = new DataListener<>() {
        @Override
        public void onData(SocketIOClient client, String isTyping, AckRequest arg2) {
            log.info("++++++++++++++++++++");
            if (Boolean.parseBoolean(isTyping))
                namespace.getBroadcastOperations().sendEvent("userTyping", client);
            else
                namespace.getBroadcastOperations().sendEvent("userStopTyping", client);
        }
    };

    private String generateRoomId(String userId1, String userId2) {
        List<String> ids = Arrays.asList(userId1, userId2);
        Collections.sort(ids);
        return ids.get(0) + "_" + ids.get(1);
    }
}