package org.example.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.example.domain.FriendshipOffer;
import org.example.domain.FriendshipOfferRepo;
import org.example.domain.KafkaFriendshipRequest;
import org.example.domain.KafkaFriendshipResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class FriendshipService {

    private final FriendshipOfferRepo friendshipOfferRepo;
    private final ReplyingKafkaTemplate<String, KafkaFriendshipRequest, KafkaFriendshipResponse> replyingKafkaTemplate;


    public String deleteFromFriends(String type,String user, String friend) throws ExecutionException, InterruptedException {
        KafkaFriendshipRequest request = new KafkaFriendshipRequest(type, user, friend);
        ProducerRecord<String, KafkaFriendshipRequest> record = new ProducerRecord<>("friendship-request-topic", request);
        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "friendship-response-topic".getBytes()));
        log.info("Отправлен объект: " + record);
        RequestReplyFuture<String, KafkaFriendshipRequest, KafkaFriendshipResponse> futureResponse = replyingKafkaTemplate.sendAndReceive(record);
        log.info("Получен объект: " + futureResponse.get().value());
        KafkaFriendshipResponse response = futureResponse.get().value();
        return response.getStatus();
    }

    public void deleteByTimestampBefore(LocalDateTime timestamp) {
        List<FriendshipOffer> result = friendshipOfferRepo.findByTimestampBefore(timestamp);
        log.info("Удалены записи: " + result.toString());
        friendshipOfferRepo.deleteAll(result);
    }

    public List<FriendshipOffer> getOffersByTypeAndBelonging(String receiverId, String type, String belonging) {
        validateType(type);
        List<FriendshipOffer> offers = getOffers(receiverId, type, belonging);
        if ("accepted".equals(type) || "refused".equals(type)) {
            List<UUID> idsToDelete = offers.stream()
                    .map(FriendshipOffer::getId)
                    .collect(Collectors.toList());
            friendshipOfferRepo.deleteAllById(idsToDelete);
            log.info("Удалены записи: " + offers.toString());
        }

        log.info("Возвращены записи: " + offers.toString());
        return offers;
    }

    public void validateType(String type) throws IllegalArgumentException {
        if (!"accepted".equals(type) &&!"refused".equals(type) &&!"pending".equals(type)) {
            throw new IllegalArgumentException("Неверный тип предложения дружбы");
        }
    }


    private List<FriendshipOffer> getOffers(String Id, String type, String belonging){
        return switch (belonging) {
            case "sender" -> friendshipOfferRepo.findOffersBySender(Id, type);
            case "receiver" -> friendshipOfferRepo.findOffersByReceiver(Id, type);
            default -> throw new IllegalStateException("Unexpected value: " + belonging);
        };
    }

}
