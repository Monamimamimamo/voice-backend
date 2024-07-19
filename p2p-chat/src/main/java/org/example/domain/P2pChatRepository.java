package org.example.domain;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface P2pChatRepository extends JpaRepository<P2pChat, UUID> {

    @Query("SELECT m " +
            "FROM P2pChat m WHERE " +
            "(m.user1 = :user1) AND (m.user2 = :user2)" +
            " OR " +
            "(m.user1 = :user2) AND (m.user2 = :user1) " +
            "ORDER BY m.timestamp DESC")
    @Transactional
    P2pChat findChatByPair(@Param("user1") String user1, @Param("user2") String user2);

    @Query("SELECT m " +
            "FROM P2pChat m WHERE " +
            "(m.user1 = :user1) OR (m.user2 = :user1) " +
            "ORDER BY m.timestamp DESC")
    @Transactional
    List<P2pChat> findChatByUser(@Param("user1") String user1, Pageable pageable);
}