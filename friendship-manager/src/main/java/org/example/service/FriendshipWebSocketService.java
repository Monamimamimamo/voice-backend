package org.example.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.auth.JwtService;
import org.example.common.kafka.KafkaService;
import org.example.domain.FriendshipOffer;
import org.example.domain.FriendshipOfferRepo;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
@AllArgsConstructor
@Slf4j
public class FriendshipWebSocketService {

    private final JwtService jwtService;
    private final KafkaService kafkaService;
    private final FriendshipOfferRepo friendshipOfferRepo;

    private static final String ERROR_MESSAGE_PENDING_ALREADY_SENT = "Вы уже отправляли такой запрос на дружбу";
    private static final String ERROR_MESSAGE_OFFER_NOT_FOUND = "Не существует такого %s запроса дружбы";
    private static final String ERROR_MESSAGE_STATUS_ALREADY_SET = "Вы уже %s запрос на дружбу";
    private static final String ERROR_FRIEND_LIST_DATABASE_UPDATE = "Заявка не смогла сохраниться у второго пользователя";



    public JSONObject handleFriendshipMessage(String receiverId, String senderJwt, String status) throws ExecutionException, InterruptedException {
        String senderId = jwtService.getNameFromAuthToken(senderJwt);
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
        existingOffer = FriendshipOffer.builder().sender(senderId).receiver(receiverId).status("pending").timestamp(time).build();
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
            String response = kafkaService.sendKafkaMessage(receiverId, senderId, "accepted");
            if (Objects.equals(response, "completed")){
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
        else return returnMessageError(ERROR_MESSAGE_OFFER_NOT_FOUND, "pending or refused");
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
}