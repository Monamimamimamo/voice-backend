package org.example.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDateTime;

@Entity
@Data
public class FriendshipOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String status;

    private LocalDateTime timestamp;

    private String sender;

    private String receiver;
}
