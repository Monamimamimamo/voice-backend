package org.example.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendshipOfferRepo extends JpaRepository<FriendshipOffer, Long> {

    @Query("SELECT f FROM FriendshipOffer f WHERE f.receiver = :receiverId AND f.status = 'accepted' ORDER BY f.timestamp ASC")
    List<FriendshipOffer> findAcceptedOffersByReceiver(@Param("receiverId") String receiverId);

    @Query("SELECT f FROM FriendshipOffer f WHERE f.receiver = :receiverId AND f.status = 'refused' ORDER BY f.timestamp ASC")
    List<FriendshipOffer> findRefusedOffersByReceiver(@Param("receiverId") String receiverId);

    @Query("SELECT f FROM FriendshipOffer f WHERE f.receiver = :receiverId AND f.status = 'pending' ORDER BY f.timestamp ASC")
    List<FriendshipOffer> findPendingOffersByReceiver(@Param("receiverId") String receiverId);

    @Query("SELECT f FROM FriendshipOffer f WHERE f.sender = :senderId AND f.receiver = :receiverId")
    FriendshipOffer findBySenderAndReceiver(@Param("senderId") String senderId, @Param("receiverId") String receiverId);
}
