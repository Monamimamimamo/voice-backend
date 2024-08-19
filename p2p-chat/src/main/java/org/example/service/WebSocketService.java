package org.example.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.Message;
import org.example.domain.MessageRepo;
import org.example.domain.P2pChat;
import org.example.domain.P2pChatRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class WebSocketService {

    private final MessageRepo messageRepo;
    private final P2pChatRepository chatRepo;

    public JSONObject handleChat(Map<String, String> map, String senderId, String receiverId) {
        String zonedDateTimeUtc = LocalDateTime
                .now()
                .atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("UTC"))
                .format(new DateTimeFormatterBuilder()
                        .appendPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
                        .toFormatter());

        String content = map.getOrDefault("content", null);
        P2pChat existingChat = chatRepo.findChatByPair(senderId, receiverId);
        if (existingChat == null) {
            existingChat = new P2pChat();
            existingChat.setUser1(senderId);
            existingChat.setUser2(receiverId);
        }
        Message db_message = createMessage(content, zonedDateTimeUtc, senderId, receiverId, existingChat);
        Message savedMessage = messageRepo.save(db_message);
        existingChat.setLastMessage(savedMessage);
        chatRepo.save(existingChat);
        return convertToJsonObject(savedMessage);
    }

    private JSONObject convertToJsonObject(Message savedMessage) {
        JSONObject resultJson = new JSONObject();
        resultJson.put("id", savedMessage.getId());
        resultJson.put("content", savedMessage.getContent());
        resultJson.put("timestamp", savedMessage.getTimestamp());
        resultJson.put("sender", savedMessage.getSender());
        resultJson.put("receiver", savedMessage.getReceiver());
        log.info("Сохранена и возвращена запись: " + savedMessage);
        return resultJson;
    }

    private Message createMessage(String content, String zonedDateTimeUtc, String senderId, String receiverId, P2pChat chat){
        return Message.builder()
                .content(content)
                .timestamp(zonedDateTimeUtc)
                .sender(senderId)
                .receiver(receiverId)
                .build();
    }
}
