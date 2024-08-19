package org.example.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.KafkaService;
import org.example.common.auth.JwtService;
import org.json.simple.JSONObject;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@AllArgsConstructor
@Slf4j
@OpenAPIDefinition(info = @Info(title = "Friendship-Management API"))
public class FriendshipWebSocketHandler {

    private final KafkaService kafkaService;
    private final ObjectMapper objectMapper;
    private final JwtService jwtService;

    @MessageMapping("/friendship/{receiverId}")
    @SendTo("/topic/friendship/{receiverId}")
    public Object handleFriendshipMessage(String message, @DestinationVariable("receiverId") String receiverId) throws IOException, ExecutionException, InterruptedException {
        Map<String, Object> parsedMap = objectMapper.readValue(message, new TypeReference<>() {
        });
        String senderJwt = parsedMap.getOrDefault("sender", null).toString();
        String status = parsedMap.getOrDefault("status", null).toString();

        if (senderJwt == null || status == null)
            throw new IllegalArgumentException("senderJwt или status не могут быть null");
        String senderId = jwtService.getNameFromAuthToken(senderJwt);
        Map<String, String> request = new HashMap<>();
        request.put("receiverId", receiverId);
        request.put("senderId", senderId);
        request.put("status", status);
        log.info("Принято сообщение: " + message);
        Object response = kafkaService.sendAndReceive(request, "friendship_request_topic", "friendship_response_topic");
        log.info("Из кафки пришло: " + response.toString());
        return new JSONObject((Map) response);
    }

}