package org.example;

import org.example.domain.Message;
import org.example.domain.MessageRepo;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
public class ChatWebSocketHandler {

    private final MessageRepo messageRepo;

    public ChatWebSocketHandler(MessageRepo messageRepo) {
        this.messageRepo = messageRepo;
    }

    @MessageMapping("/chat/{senderId}/{receiverId}")
    @SendTo("/topic/chat/{senderId}/{receiverId}")
    public JSONObject handleChat(String message, @DestinationVariable("senderId") String senderId, @DestinationVariable("receiverId") String receiverId) throws ParseException, IOException {
        JSONObject parsedJson = (JSONObject) new JSONParser().parse(message);
        String content = (String) parsedJson.get("content");
        JSONObject resultJson = new JSONObject();
        resultJson.put("content", content);

        Message db_message = new Message();
        db_message.setContent(content);
        db_message.setTimestamp(LocalDateTime.now());
        db_message.setSender(senderId);
        db_message.setReceiver(receiverId);
        messageRepo.save(db_message);
        
        return resultJson;
    }

    @GetMapping("/chat/history/messages")
    public ResponseEntity<List<Message>> getMessages(
            @RequestParam(name = "senderId", required = true) String senderId,
            @RequestParam(name = "receiverId", required = true) String receiverId,
            @RequestParam(name = "page", required = true) Integer page,
            @RequestParam(name = "length", required = true) int length) {

        if (length <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Pageable pageable = PageRequest.of(page, length);

        List<Message> messages = messageRepo.findMessagesBySenderOrReceiver(senderId, receiverId, pageable);
        if (messages.isEmpty()) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        }

        return new ResponseEntity<>(messages, HttpStatus.OK);
    }
}
