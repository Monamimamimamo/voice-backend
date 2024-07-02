package org.example.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.FriendshipOffer;
import org.example.domain.FriendshipOfferRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class FriendshipService {

    private final FriendshipOfferRepo friendshipOfferRepo;

    @Autowired
    public FriendshipService(FriendshipOfferRepo friendshipOfferRepo) {
        this.friendshipOfferRepo = friendshipOfferRepo;
    }

    public List<FriendshipOffer> getOffersByTypeAndBelonging(String receiverId, String type, String belonging) {
        validateType(type);
        return getOffers(receiverId, type, belonging);
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
