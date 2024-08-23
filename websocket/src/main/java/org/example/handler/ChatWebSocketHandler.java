package org.example.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.KafkaService;
<<<<<<< HEAD
import org.example.KafkaTopics;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
=======
>>>>>>> 5860067 (websocket handshake auth interceptor added)
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@AllArgsConstructor
@Slf4j
public class ChatWebSocketHandler {

    private final KafkaService kafkaService;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;


    @MessageMapping("/chat/{senderId}/{receiverId}")
    @SendTo("/topic/chat/{senderId}/{receiverId}")
    public Object handleChat(String message, @DestinationVariable("senderId") String senderId, @DestinationVariable("receiverId") String receiverId) throws IOException, ExecutionException, InterruptedException {
        Map<String, Object> map = objectMapper.readValue(message, new TypeReference<>() {});
        map.put("sender", senderId);
        map.put("receiver", receiverId);
        log.info("Пришло сообщение: " + map);
        return kafkaService.chatSendAndReceive(map);
    }
}
