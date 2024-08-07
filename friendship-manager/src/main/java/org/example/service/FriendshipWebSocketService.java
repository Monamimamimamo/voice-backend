package org.example.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.auth.JwtService;
import org.example.common.kafka.KafkaService;
import org.example.domain.FriendshipOffer;
import org.example.domain.FriendshipOfferRepo;
import org.json.simple.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
@AllArgsConstructor
@Slf4j
public class FriendshipWebSocketService {

    private final JwtService jwtService;
    private final KafkaService kafkaService;
    private final FriendshipOfferRepo friendshipOfferRepo;
    private static final RestTemplate restTemplate = new RestTemplate();

    private static final String ERROR_MESSAGE_PENDING_ALREADY_SENT = "Вы уже отправляли такой запрос на дружбу";
    private static final String ERROR_MESSAGE_OFFER_NOT_FOUND = "Не существует такого %s запроса дружбы";
    private static final String ERROR_MESSAGE_STATUS_ALREADY_SET = "Вы уже %s запрос на дружбу";
    private static final String ERROR_FRIEND_LIST_DATABASE_UPDATE = "Заявка не смогла сохраниться у второго пользователя";
    private static final String ERROR_MYSELF_OFFER = "Заявка отправлена самому себе";



    public JSONObject handleFriendshipMessage(String receiverId, String senderJwt, String status) throws ExecutionException, InterruptedException {
        String senderId = jwtService.getNameFromAuthToken(senderJwt);
        FriendshipOffer existingOffer = friendshipOfferRepo.findBySenderAndReceiver(senderId, receiverId).orElse(friendshipOfferRepo.findByReceiverAndSender(senderId, receiverId).orElse(null));
        LocalDateTime time = LocalDateTime.now();
        return switch (status) {
            case "pending" -> handlePendingStatus(existingOffer, senderId, receiverId, time);
            case "accepted" -> handleAcceptedStatus(existingOffer, senderId, receiverId, time);
            case "refused" -> handleRefusedStatus(existingOffer, senderId, receiverId, time);
            default -> returnMessageError("Команда не опознана");
        };
    }

    private JSONObject handlePendingStatus(FriendshipOffer existingOffer, String senderId, String receiverId, LocalDateTime time) {
        if (existingOffer!= null)
            return returnMessageError(ERROR_MESSAGE_PENDING_ALREADY_SENT);
        if (Objects.equals(senderId, receiverId))
            return returnMessageError(ERROR_MYSELF_OFFER);
        existingOffer = FriendshipOffer.builder().sender(senderId).receiver(receiverId).status("pending").timestamp(time).build();
        friendshipOfferRepo.save(existingOffer);
        log.info("Сохранена запись: " + existingOffer);
        return convertToJsonObject(existingOffer, senderId);
    }

    private JSONObject handleAcceptedStatus(FriendshipOffer existingOffer, String senderId, String receiverId, LocalDateTime time) throws ExecutionException, InterruptedException {
        if (existingOffer == null)
            return returnMessageError(ERROR_MESSAGE_OFFER_NOT_FOUND, "pending");
        else if (existingOffer.getStatus().equals("accepted"))
            return returnMessageError(ERROR_MESSAGE_STATUS_ALREADY_SET, "accepted");
        else {
            //String response = kafkaService.sendKafkaMessage(receiverId, senderId, "accepted");
            //HTTP :)
            StringBuilder sb = new StringBuilder();
            String url = sb.append("https://voice-backend.ru:8083/api/Order/AddFriend?user=")
                    .append(senderId)
                    .append("&friend=")
                    .append(receiverId)
                    .toString();
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class);

            String response = responseEntity.getBody();
            // :)
            if (Objects.equals(response, "All good")){
                existingOffer.setTimestamp(time);
                existingOffer.setStatus("accepted");
                friendshipOfferRepo.save(existingOffer);
                log.info("Сохранена запись: " + existingOffer);
                return convertToJsonObject(existingOffer, senderId);
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
            return convertToJsonObject(existingOffer, senderId);
        }
    }


    public JSONObject convertToJsonObject(FriendshipOffer existingOffer, String sender) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "friendRequest");
        jsonObject.put("id", existingOffer.getId());
        jsonObject.put("sender", sender);
        jsonObject.put("timestamp", existingOffer.getTimestamp().toString());
        jsonObject.put("status", existingOffer.getStatus());
        return jsonObject;
    }


    public static JSONObject returnMessageError(String message) {
        JSONObject resultJson = new JSONObject();
        resultJson.put("type", "error");
        resultJson.put("body", message);
        return resultJson;
    }

    public static JSONObject returnMessageError(String message, String type) {
        JSONObject resultJson = new JSONObject();
        resultJson.put("type", "error");
        resultJson.put("body", String.format(message, type));
        return resultJson;
    }
}