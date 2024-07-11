package org.example.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.kafka.KafkaService;
import org.example.domain.FriendshipOffer;
import org.example.domain.FriendshipOfferRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class FriendshipService {

    private final FriendshipOfferRepo friendshipOfferRepo;
    private final KafkaService kafkaService;


    public String deleteFromFriends(String user, String friend, String type) throws ExecutionException, InterruptedException {
        return kafkaService.sendKafkaMessage(user, friend, type);
    }

    public void deleteByTimestampBefore(LocalDateTime timestamp) {
        friendshipOfferRepo.deleteByTimestampBefore(timestamp);
    }

    public List<FriendshipOffer> getOffersByTypeAndBelonging(String receiverId, String type, String belonging) {
        validateType(type);
        List<FriendshipOffer> offers = getOffers(receiverId, type, belonging);
        if ("accepted".equals(type) || "refused".equals(type)) {
            List<UUID> idsToDelete = offers.stream()
                    .map(FriendshipOffer::getId)
                    .collect(Collectors.toList());
            friendshipOfferRepo.deleteAllById(idsToDelete);
            log.info("Удалены записи: " + offers);
        }

        log.info("Возвращены записи: " + offers.toString());
        return offers;
    }

    public void validateType(String type) throws IllegalArgumentException {
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
