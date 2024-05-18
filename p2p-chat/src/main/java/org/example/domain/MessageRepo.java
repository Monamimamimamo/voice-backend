package org.example.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepo extends JpaRepository<Message, Long> {
    @Query("SELECT m " +
            "FROM Message m WHERE " +
            "(m.sender = :senderId) AND (m.receiver = :receiverId)" +
            " OR " +
            "(m.sender = :receiverId) AND (m.receiver = :senderId) " +
            "ORDER BY m.timestamp DESC")
    List<Message> findMessagesBySenderOrReceiver(@Param("senderId") String senderId, @Param("receiverId") String receiverId, Pageable pageable);
}
