package org.example.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.domain.Message;
import org.example.domain.MessageRepo;
import org.example.service.WebSocketService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@AllArgsConstructor
public class ChatWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final WebSocketService webSocketService;


    @Transactional
    @MessageMapping("/chat/{senderId}/{receiverId}")
    @SendTo("/topic/chat/{senderId}/{receiverId}")
    public JSONObject handleChat(String message, @DestinationVariable("senderId") String senderId, @DestinationVariable("receiverId") String receiverId) throws ParseException, IOException {
        Map<String, Object> map = objectMapper.readValue(message, new TypeReference<>() {});
        return webSocketService.handleChat(map, senderId, receiverId);
    }
}
