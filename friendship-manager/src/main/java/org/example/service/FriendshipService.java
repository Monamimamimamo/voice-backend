package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.KafkaService;
import org.example.common.auth.JwtService;
//import org.example.common.kafka.KafkaService;
import org.example.domain.FriendshipOffer;
import org.example.domain.FriendshipOfferRepo;
import org.example.domain.OperationStatus;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
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
    private final KafkaService kafkaService;
    private final JwtService jwtService;


    public ResponseEntity<FriendshipOffer> createOffer(String receiver, HttpServletRequest request) throws ExecutionException, InterruptedException {
        String sender = jwtService.extractUserName(request);
        FriendshipOffer existingOffer = friendshipOfferRepo.findBySenderAndReceiver(sender, receiver).orElse(friendshipOfferRepo.findByReceiverAndSender(sender, receiver).orElse(null));
        LocalDateTime time = LocalDateTime.now();
        if (existingOffer != null)
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        if (Objects.equals(sender, receiver))
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        existingOffer = FriendshipOffer.builder().sender(sender).receiver(receiver).status("pending").timestamp(time).build();
        friendshipOfferRepo.save(existingOffer);
        log.info("Сохранена запись: " + existingOffer);
        Map<String, String> response = new HashMap<>();
        response.put("receiver", existingOffer.getReceiver());
        response.put("sender", existingOffer.getSender());
        response.put("status", existingOffer.getStatus());
        kafkaService.friendshipSendToNotification(response);
        return ResponseEntity.ok(existingOffer);
    }

    public ResponseEntity<FriendshipOffer> acceptOffer(String sender, HttpServletRequest request){
        String receiver = jwtService.extractUserName(request);
        FriendshipOffer existingOffer = friendshipOfferRepo.findBySenderAndReceiver(sender, receiver).orElse(friendshipOfferRepo.findByReceiverAndSender(sender, receiver).orElse(null));
        LocalDateTime time = LocalDateTime.now();
        if (existingOffer == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        else if (existingOffer.getStatus().equals("accepted"))
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        else {
            if (!Objects.equals(existingOffer.getSender(), sender))
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            String response = kafkaService.sendKafkaMessage(receiverId, senderId, "accepted");
            //HTTP :)
            StringBuilder sb = new StringBuilder();
            String url = sb.append("https://voice-backend.ru:8083/api/Order/AddFriend?user=")
                    .append(sender)
                    .append("&friend=")
                    .append(receiver)
                    .toString();
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class).getBody();
//
//            String operationStatus = responseEntity.getBody();
            // :)
            if (Objects.equals(response, "completed")) {
                existingOffer.setTimestamp(time);
                existingOffer.setStatus("accepted");
                friendshipOfferRepo.save(existingOffer);
                log.info("Сохранена запись: " + existingOffer);
                // TODO отправить мессагу в нотификации
                return ResponseEntity.ok(existingOffer);
            } else {
                return new ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE);
            }
        }
    }

    public ResponseEntity<FriendshipOffer> refuseOffer(@RequestParam String sender,
                                                       HttpServletRequest request){
        String receiver = jwtService.extractUserName(request);
        FriendshipOffer existingOffer = friendshipOfferRepo.findBySenderAndReceiver(sender, receiver).orElse(friendshipOfferRepo.findByReceiverAndSender(sender, receiver).orElse(null));
        LocalDateTime time = LocalDateTime.now();
        if (existingOffer == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        else if (existingOffer.getStatus().equals("refused"))
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        else {
            if (!Objects.equals(existingOffer.getSender(), sender))
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            existingOffer.setTimestamp(time);
            existingOffer.setStatus("refused");
            friendshipOfferRepo.save(existingOffer);
            log.info("Сохранена запись: " + existingOffer);
            // TODO отправить мессагу в нотификации
            return ResponseEntity.ok(existingOffer);
        }
    }

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
