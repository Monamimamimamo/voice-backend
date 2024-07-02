package org.example.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.domain.FriendshipOffer;
import org.example.domain.FriendshipOfferRepo;
import org.example.domain.KafkaFriendshipMessage;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class FriendshipWebSocketService {

    private final AuthService authService;

    private static final String ERROR_MESSAGE_PENDING_ALREADY_SENT = "Вы уже отправляли такой запрос на дружбу";
    private static final String ERROR_MESSAGE_OFFER_NOT_FOUND = "Не существует такого ожидающего запроса дружбы";
    private static final String ERROR_MESSAGE_STATUS_ALREADY_SET = "Вы уже %s запрос на дружбу";


    private final FriendshipOfferRepo friendshipOfferRepo;
    private final ReplyingKafkaTemplate<String, KafkaFriendshipMessage, KafkaFriendshipMessage> replyingKafkaTemplate;

    public JSONObject handleFriendshipMessage(String receiverId, String senderJwt, String status){
        String senderId = authService.getNameFromAuthToken(senderJwt);
        log.info("sender: " + senderId);
        log.info("receiver: " + receiverId);
        FriendshipOffer existingOffer = friendshipOfferRepo.findBySenderAndReceiver(senderId, receiverId).orElse(friendshipOfferRepo.findByReceiverAndSender(senderId, receiverId).orElse(null));
        LocalDateTime time = LocalDateTime.now();
        return switch (status) {
            case "pending" -> handlePendingStatus(existingOffer, senderId, receiverId, time);
            case "accepted", "refused" -> handleAcceptedOrRefusedStatus(existingOffer, senderId, receiverId, time, status);
            default -> returnMessageError("Команда не опознана");
        };
    }

    private JSONObject handlePendingStatus(FriendshipOffer existingOffer, String senderId, String receiverId, LocalDateTime time) {
        if (existingOffer!= null)
            return returnMessageError(ERROR_MESSAGE_PENDING_ALREADY_SENT);
        existingOffer = FriendshipOffer.builder()
                .sender(senderId)
                .receiver(receiverId)
                .status("pending")
                .timestamp(time)
                .build();
        friendshipOfferRepo.save(existingOffer);
        return convertToJsonObject(existingOffer);
    }

    private JSONObject handleAcceptedOrRefusedStatus(FriendshipOffer existingOffer, String senderId, String receiverId, LocalDateTime time, String status) {
        if (existingOffer == null)
            return returnMessageError(ERROR_MESSAGE_OFFER_NOT_FOUND);
        else if (existingOffer.getStatus().equals(status))
            return returnMessageError(ERROR_MESSAGE_STATUS_ALREADY_SET);
        else {
            existingOffer.setTimestamp(time);
            existingOffer.setStatus(status);
            sendKafkaMessage(receiverId, senderId, status);
            friendshipOfferRepo.save(existingOffer);
            return convertToJsonObject(existingOffer);
        }
    }

    public JSONObject convertToJsonObject(FriendshipOffer existingOffer) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", existingOffer.getId());
        jsonObject.put("timestamp", existingOffer.getTimestamp().toString());
        jsonObject.put("status", existingOffer.getStatus());
        return jsonObject;
    }


    public static JSONObject returnMessageError(String message) {
        JSONObject resultJson = new JSONObject();
        resultJson.put("error", message);
        return resultJson;
    }

    public void sendKafkaMessage(String receiverId, String senderId, String type) {
        KafkaFriendshipMessage kafkaFriendshipMessage = new KafkaFriendshipMessage(type, receiverId, senderId);
        ProducerRecord<String, KafkaFriendshipMessage> record = new ProducerRecord<>("friendship-topic", kafkaFriendshipMessage);
        replyingKafkaTemplate.send(record);
    }



}