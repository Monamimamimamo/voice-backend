package org.example.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

public interface FriendshipOfferRepo extends JpaRepository<FriendshipOffer, UUID> {

    Optional<FriendshipOffer> findBySenderAndReceiver(String sender, String receiver);

    Optional<FriendshipOffer> findByReceiverAndSender(String receiver, String sender);



    @Query("SELECT f FROM FriendshipOffer f WHERE f.timestamp < :timestamp")
    List<FriendshipOffer> findByTimestampBefore(@Param("timestamp") LocalDateTime timestamp);


    @Query(value = "SELECT * " +
            "FROM friendship_offer f " +
            "WHERE f.status = :status " +
            "AND f.receiver = :receiverId " +
            "ORDER BY f.timestamp " +
            "ASC", nativeQuery = true)
    List<FriendshipOffer> findOffersByReceiver(@Param("receiverId") String receiverId, @Param("status") String status);

    @Query(value = "SELECT * " +
            "FROM friendship_offer f " +
            "WHERE f.status = :status " +
            "AND f.sender = :senderId " +
            "ORDER BY f.timestamp " +
            "ASC", nativeQuery = true)
    List<FriendshipOffer> findOffersBySender(@Param("senderId") String senderId, @Param("status") String status);
}
