package org.example.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.example.domain.FriendshipOffer;
import org.example.domain.FriendshipOfferRepo;
import org.example.domain.KafkaFriendshipRequest;
import org.example.domain.KafkaFriendshipResponse;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
@AllArgsConstructor
@Slf4j
public class FriendshipWebSocketService {

    private final AuthService authService;

    private static final String ERROR_MESSAGE_PENDING_ALREADY_SENT = "Вы уже отправляли такой запрос на дружбу";
    private static final String ERROR_MESSAGE_OFFER_NOT_FOUND = "Не существует такого %s запроса дружбы";
    private static final String ERROR_MESSAGE_STATUS_ALREADY_SET = "Вы уже %s запрос на дружбу";
    private static final String ERROR_FRIEND_LIST_DATABASE_UPDATE = "Заявка не смогла сохраниться у второго пользователя";


    private final FriendshipOfferRepo friendshipOfferRepo;
    private final ReplyingKafkaTemplate<String, KafkaFriendshipRequest, KafkaFriendshipResponse> replyingKafkaTemplate;

    public JSONObject handleFriendshipMessage(String receiverId, String senderJwt, String status) throws ExecutionException, InterruptedException {
        String senderId = authService.getNameFromAuthToken(senderJwt);
        log.info("sender: " + senderId);
        log.info("receiver: " + receiverId);
        FriendshipOffer existingOffer = friendshipOfferRepo.findBySenderAndReceiver(senderId, receiverId).orElse(friendshipOfferRepo.findByReceiverAndSender(senderId, receiverId).orElse(null));
        LocalDateTime time = LocalDateTime.now();
        return switch (status) {
            case "pending" -> handlePendingStatus(existingOffer, senderId, receiverId, time);
            case "accepted" -> handleAcceptedStatus(existingOffer, senderId, receiverId, time);
            case "refused" -> handleRefusedStatus(existingOffer, senderId, receiverId, time);
            case "checked" -> handleCheckedStatus(existingOffer, senderId, receiverId, time);
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
        log.info("Сохранена запись: " + existingOffer);
        return convertToJsonObject(existingOffer);
    }

    private JSONObject handleAcceptedStatus(FriendshipOffer existingOffer, String senderId, String receiverId, LocalDateTime time) throws ExecutionException, InterruptedException {
        if (existingOffer == null)
            return returnMessageError(ERROR_MESSAGE_OFFER_NOT_FOUND, "pending");
        else if (existingOffer.getStatus().equals("accepted"))
            return returnMessageError(ERROR_MESSAGE_STATUS_ALREADY_SET, "accepted");
        else {
            boolean response = sendKafkaMessage(receiverId, senderId, "accepted");
            if (response){
                existingOffer.setTimestamp(time);
                existingOffer.setStatus("accepted");
                friendshipOfferRepo.save(existingOffer);
                log.info("Сохранена запись: " + existingOffer);
                return convertToJsonObject(existingOffer);
            } else {
                return returnMessageError(ERROR_FRIEND_LIST_DATABASE_UPDATE);
            }
        }
    }

    private JSONObject handleRefusedStatus(FriendshipOffer existingOffer, String senderId, String receiverId, LocalDateTime time) throws ExecutionException, InterruptedException {
        if (existingOffer == null)
            return returnMessageError(ERROR_MESSAGE_OFFER_NOT_FOUND, "pending");
        else if (existingOffer.getStatus().equals("refused"))
            return returnMessageError(ERROR_MESSAGE_STATUS_ALREADY_SET, "refused");
        else {
            existingOffer.setTimestamp(time);
            existingOffer.setStatus("refused");
            friendshipOfferRepo.save(existingOffer);
            log.info("Сохранена запись: " + existingOffer);
            return convertToJsonObject(existingOffer);
        }
    }

    private JSONObject handleCheckedStatus(FriendshipOffer existingOffer, String senderId, String receiverId, LocalDateTime time) {
        if (existingOffer == null)
            return returnMessageError(ERROR_MESSAGE_OFFER_NOT_FOUND, "pending");
        else if (existingOffer.getStatus().equals("refused") || existingOffer.getStatus().equals("accepted")){
            friendshipOfferRepo.deleteById(existingOffer.getId());
            log.info("Удалена запись: " + existingOffer);
            existingOffer.setStatus("deleted");
            return convertToJsonObject(existingOffer);
        }
        else
            return returnMessageError(ERROR_MESSAGE_OFFER_NOT_FOUND, "pending or refused");
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

    public static JSONObject returnMessageError(String message, String type) {
        JSONObject resultJson = new JSONObject();
        resultJson.put("error", String.format(message, type));
        return resultJson;
    }

    public boolean sendKafkaMessage(String receiverId, String senderId, String type) throws ExecutionException, InterruptedException {
        KafkaFriendshipRequest request = new KafkaFriendshipRequest(type, receiverId, senderId);
        ProducerRecord<String, KafkaFriendshipRequest> record = new ProducerRecord<>("friendship-request-topic", receiverId, request);
        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "friendship-response-topic".getBytes()));
        log.info("Отправлен объект: " + record);
        RequestReplyFuture<String, KafkaFriendshipRequest, KafkaFriendshipResponse> futureResponse = replyingKafkaTemplate.sendAndReceive(record);
        log.info("Получен объект: " + futureResponse.get().value());
        KafkaFriendshipResponse response = futureResponse.get().value();
        return Objects.equals(response.getStatus(), "completed");
    }
}