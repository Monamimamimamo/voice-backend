package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.auth.JwtService;
//import org.example.common.kafka.KafkaService;
import org.example.domain.FriendshipOffer;
import org.example.domain.FriendshipOfferRepo;
import org.example.domain.OperationStatus;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class FriendshipService {
    private static final RestTemplate restTemplate = new RestTemplate();

    private final FriendshipOfferRepo friendshipOfferRepo;
//    private final KafkaService kafkaService;
    private final JwtService jwtService;

    public ResponseEntity<OperationStatus> deletePendingOffer(HttpServletRequest request, String receiver){
        try {
            String userName = jwtService.extractUserName(request);
            int affectedRows = friendshipOfferRepo.deletePendingOfferBySenderAndReceiver(userName, receiver);
            if (affectedRows == 0)
                return ResponseEntity.notFound().build();
            return ResponseEntity.ok().body(new OperationStatus("success"));
        } catch (Exception e) {
            log.error("Ошибка при удалении предложения дружбы", e);
            return ResponseEntity.badRequest().body(new OperationStatus("something went wrong"));
        }
    }


    public String deleteFromFriends(String user, String friend, String type) throws ExecutionException, InterruptedException {
        return null;
//        return kafkaService.sendKafkaMessage(user, friend, type);
    }

    public void deleteByTimestampBefore(LocalDateTime timestamp) {
        friendshipOfferRepo.deleteByTimestampBefore(timestamp);
    }

    public List<Map<String, Object>> getOffersByTypeAndBelonging(String userId, String type, String belonging, String token) {
        validateType(type);
        List<FriendshipOffer> offers = getOffers(userId, type, belonging);
        if ("accepted".equals(type) || "refused".equals(type)) {
            List<UUID> idsToDelete = offers.stream()
                    .map(FriendshipOffer::getId)
                    .collect(Collectors.toList());
            friendshipOfferRepo.deleteAllById(idsToDelete);
            log.info("Удалены записи: " + offers);
        }
        List<Map<String, Object>> users = new ArrayList<>();
        for (FriendshipOffer offer : offers) {
            StringBuilder sb = new StringBuilder();
            String url = sb.append("https://voice-backend.ru:8083/api/Order/GetUser?friendName=")
                    .append(belonging.equals("sender") ? offer.getReceiver() : offer.getSender())
                    .append("&page=1&pageSize=1000000000").toString();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class);

            Map<String, Object> user = parseJsonToUsers(responseEntity.getBody());
            users.add(user);
        }

        log.info("Возвращены записи: " + offers.toString());
        return users;
    }

    private Map<String, Object> parseJsonToUsers(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>(){}).getFirst();
        } catch (IOException e) {
            log.error("Ошибка при парсинге JSON", e);
            return Collections.emptyMap();
        }
    }

    private void validateType(String type) throws IllegalArgumentException {
        if (!"accepted".equals(type) &&!"refused".equals(type) &&!"pending".equals(type))
            throw new IllegalArgumentException("Неверный тип предложения дружбы");
    }


    private List<FriendshipOffer> getOffers(String Id, String type, String belonging){
        return switch (belonging) {
            case "sender" -> friendshipOfferRepo.findOffersBySender(Id, type);
            case "receiver" -> friendshipOfferRepo.findOffersByReceiver(Id, type);
            default -> throw new IllegalStateException("Unexpected value: " + belonging);
        };
    }

}
