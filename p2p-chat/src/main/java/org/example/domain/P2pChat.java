package org.example.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"user1", "user2"})})
@Data
public class P2pChat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lastMessage", referencedColumnName = "id")
    private Message lastMessage;

    @NotNull
    private String user1;

    @NotNull
    private String user2;

}
