package org.example.service;

import org.example.domain.FriendshipOffer;
import org.example.domain.FriendshipOfferRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendshipService {

    private final FriendshipOfferRepo friendshipOfferRepo;

    public FriendshipService(FriendshipOfferRepo friendshipOfferRepo) {
        this.friendshipOfferRepo = friendshipOfferRepo;
    }

    public List<FriendshipOffer> getAcceptedOffers(String receiverId){
        return friendshipOfferRepo.findAcceptedOffersByReceiver(receiverId);
    }

    public List<FriendshipOffer> getRefusedOffers(String receiverId){
        return friendshipOfferRepo.findRefusedOffersByReceiver(receiverId);
    }

    public List<FriendshipOffer> getPendingOffers(String receiverId){
        return friendshipOfferRepo.findPendingOffersByReceiver(receiverId);
    }
}
