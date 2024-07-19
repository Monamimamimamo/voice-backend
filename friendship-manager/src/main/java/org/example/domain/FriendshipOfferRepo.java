package org.example.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

public interface FriendshipOfferRepo extends JpaRepository<FriendshipOffer, UUID> {

    @Modifying
    @Transactional
    @Query("DELETE FROM FriendshipOffer fo WHERE fo.sender = :sender AND fo.receiver = :receiver AND fo.status = 'pending'")
    int deletePendingOfferBySenderAndReceiver(@Param("sender") String sender, @Param("receiver") String receiver);

    Optional<FriendshipOffer> findBySenderAndReceiver(String sender, String receiver);

    Optional<FriendshipOffer> findByReceiverAndSender(String receiver, String sender);



    @Query("DELETE FROM FriendshipOffer fo WHERE fo.timestamp < :timestamp")
    void deleteByTimestampBefore(@Param("timestamp") LocalDateTime timestamp);



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
