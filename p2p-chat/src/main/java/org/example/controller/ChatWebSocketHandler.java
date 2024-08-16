package org.example.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.WebSocketService;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@AllArgsConstructor
@Slf4j
public class ChatWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final WebSocketService webSocketService;


    @Transactional
    @MessageMapping("/chat/{senderId}/{receiverId}")
    @SendTo("/topic/chat/{senderId}/{receiverId}")
    public JSONObject handleChat(String message, @DestinationVariable("senderId") String senderId, @DestinationVariable("receiverId") String receiverId) throws ParseException, IOException {
        Map<String, Object> map = objectMapper.readValue(message, new TypeReference<>() {});
        log.info("Пришло сообщение: " + map);
        return webSocketService.handleChat(map, senderId, receiverId);
    }
}
