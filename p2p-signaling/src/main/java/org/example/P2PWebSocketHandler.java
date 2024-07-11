package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;


@RestController
@Slf4j
public class P2PWebSocketHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @MessageMapping("/signaling/{senderId}/{receiverId}")
    @SendTo("/topic/signaling/{senderId}/{receiverId}")
    public String handleChat(String message, @DestinationVariable("senderId") String senderId, @DestinationVariable("receiverId") String receiverId) throws IOException, ParseException {
        Map<String, Object> map = objectMapper.readValue(message, new TypeReference<>() {});
        String type = map.getOrDefault("type", null).toString();
        log.info("Обработано сообщение: " + map);
        return switch (type) {
            case "offer", "answer", "candidate" -> message;
            default -> throw new IllegalStateException("Unexpected value (type): " + type);
        };
    }
}
