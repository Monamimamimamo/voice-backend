package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.Message;
import org.example.domain.MessageRepo;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
public class WebSocketService {

    @Autowired
    private MessageRepo messageRepo;

    public JSONObject handleChat(Map<String, Object> map, String senderId, String receiverId) {

        String content = map.getOrDefault("content", null).toString();
        Message db_message = Message.builder()
                .content(content)
                .timestamp(LocalDateTime.now())
                .sender(senderId)
                .receiver(receiverId)
                .build();
        Message savedMessage = messageRepo.save(db_message);
        return convertToJsonObject(savedMessage);
    }

    public JSONObject convertToJsonObject(Message savedMessage) {
        JSONObject resultJson = new JSONObject();
        resultJson.put("id", savedMessage.getId());
        resultJson.put("content", savedMessage.getContent());
        resultJson.put("timestamp", savedMessage.getTimestamp());
        resultJson.put("sender", savedMessage.getSender());
        resultJson.put("receiver", savedMessage.getReceiver());
        log.info("Сохранена и возвращена запись: " + savedMessage);
        return resultJson;
    }
}
