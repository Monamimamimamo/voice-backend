package org.example;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.example.domain.Message;
import org.example.domain.MessageRepo;
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
import java.util.Objects;

@RestController
public class ChatWebSocketHandler {

    @Autowired
    private MessageRepo messageRepo;


    @MessageMapping("/chat/{senderId}/{receiverId}")
    @SendTo("/topic/chat/{senderId}/{receiverId}")
    public JSONObject handleChat(String message, @DestinationVariable("senderId") String senderId, @DestinationVariable("receiverId") String receiverId) throws ParseException, IOException {
        JSONObject parsedJson = (JSONObject) new JSONParser().parse(message);
        String content = (String) parsedJson.get("content");

        Message db_message = new Message();
        db_message.setContent(content);
        db_message.setTimestamp(LocalDateTime.now());
        db_message.setSender(senderId);
        db_message.setReceiver(receiverId);
        Message savedMessage = messageRepo.save(db_message);


        JSONObject resultJson = new JSONObject();

        // Добавляем информацию о сообщении в resultJson
        resultJson.put("id", savedMessage.getId()); // Текст сообщения
        resultJson.put("content", content); // Текст сообщения
        resultJson.put("timestamp", LocalDateTime.now()); // Временная метка отправки
        resultJson.put("sender", senderId); // Идентификатор отправителя
        resultJson.put("receiver", receiverId); // Идентификатор получателя
        return resultJson;
    }
}
