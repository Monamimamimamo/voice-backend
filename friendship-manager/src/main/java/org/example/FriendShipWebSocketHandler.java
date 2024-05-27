package org.example;

import lombok.AllArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.domain.FriendshipOffer;
import org.example.domain.FriendshipOfferRepo;
import org.example.domain.KafkaFriendshipMessage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RestController
@AllArgsConstructor
public class FriendShipWebSocketHandler {

    private final FriendshipOfferRepo friendshipOfferRepo;
    private final ReplyingKafkaTemplate<String, KafkaFriendshipMessage, KafkaFriendshipMessage> replyingKafkaTemplate;

    @MessageMapping("/friendship/{receiverId}")
    @SendTo("/topic/friendship/{receiverId}")
    public JSONObject handleChat(String message, @DestinationVariable("receiverId") String receiverId) throws ParseException, IOException {
        JSONObject parsedJson = (JSONObject) new JSONParser().parse(message);
        String senderId = (String) parsedJson.get("sender");
        String status = (String) parsedJson.get("status");

        JSONObject resultJson = new JSONObject();
        LocalDateTime time = LocalDateTime.now();
        resultJson.put("timestamp", time);
        resultJson.put("sender", senderId);
        resultJson.put("receiver", receiverId);
        resultJson.put("status", status);

        FriendshipOffer friendshipOffer = new FriendshipOffer();
        if (Objects.equals(status, "pending")){
            friendshipOffer.setTimestamp(time);
            friendshipOffer.setSender(senderId);
            friendshipOffer.setReceiver(receiverId);
            friendshipOffer.setStatus(status);
        } else if (Objects.equals(status, "accepted")) {
            friendshipOffer = friendshipOfferRepo.findFirstBySenderAndReceiver(receiverId, senderId);
            if(Objects.equals(friendshipOffer.getStatus(), "pending")){
                friendshipOffer.setStatus(status);
            }
            KafkaFriendshipMessage kafkaFriendshipMessage = new KafkaFriendshipMessage();
            kafkaFriendshipMessage.setReceiver(receiverId);
            kafkaFriendshipMessage.setSender(senderId);
            ProducerRecord<String, KafkaFriendshipMessage> record = new ProducerRecord<>("friendship-topic", kafkaFriendshipMessage);
            replyingKafkaTemplate.send(record);
            System.out.println("Отправлено: " + record);
        } else if (Objects.equals(status, "refused")) {
            friendshipOffer = friendshipOfferRepo.findFirstBySenderAndReceiver(receiverId, senderId);
            if(Objects.equals(friendshipOffer.getStatus(), "pending")){
                friendshipOffer.setStatus(status);
            }
        }
        friendshipOfferRepo.save(friendshipOffer);
        System.out.println("Сохранено: " + friendshipOffer);
        return resultJson;
    }
}
