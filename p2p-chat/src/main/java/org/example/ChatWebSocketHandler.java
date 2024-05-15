package org.example;

import org.json.simple.JSONObject;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatWebSocketHandler {

    @MessageMapping("/chat/{userId1}/{userId2}")
    @SendTo("/topic/chat/{userId1}/{userId2}")
    public String handleChat(String message, @DestinationVariable("userId1") String userId1, @DestinationVariable("userId2") String userId2) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", message); // Добавляем ключ "message" с значением message
        return jsonObject.toString(); // Теперь мы явно приводим JSONObject к строке
    }
}
