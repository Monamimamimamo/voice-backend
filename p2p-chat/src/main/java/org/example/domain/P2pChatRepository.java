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

    @Query(value = "SELECT * FROM p2p_chat WHERE " +
            "(user1 = :user1 AND user2 = :user2)" +
            " OR " +
            "(user1 = :user2 AND user2 = :user1)", nativeQuery = true)
    @Transactional
    P2pChat findChatByPair(@Param("user1") String user1, @Param("user2") String user2);


    @Query(value = "SELECT * " +
            "FROM p2p_chat WHERE " +
            "(user1 = :user1 OR user2 = :user1)", nativeQuery = true)
    @Transactional
    List<P2pChat> findChatByUser(@Param("user1") String user1, Pageable pageable);
}