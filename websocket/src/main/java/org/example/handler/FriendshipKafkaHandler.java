package org.example.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.KafkaService;
import org.example.common.auth.JwtService;
import org.json.simple.JSONObject;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@AllArgsConstructor
@Slf4j
public class FriendshipKafkaHandler {

    private final SimpMessagingTemplate websocketTemplate;

    @KafkaListener(topics = "friendship_request_topic", groupId = "group_id1")
    public void handleFriendshipMessage(ConsumerRecord<Object, Object>  record) {
        log.info(STR."Из кафка пришло сообщение: \{record.toString()}");
//        Map<String, Object> map = record.value();
//        Map<String, String> response = new HashMap<>();
//        response.put("type", "friendshipOffer");
//        response.put("status", map.get("status").toString());
//        response.put("sender", map.get("sender").toString());
//        websocketTemplate.convertAndSend("/notification/" + map.get("receiver"), response);
//        log.info("Отправляем вебсокет-сообщение на топик: " + "/notification/" + map.get("receiver"));
    }
}