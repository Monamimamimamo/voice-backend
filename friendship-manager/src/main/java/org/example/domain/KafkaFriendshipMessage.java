package org.example.domain;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class KafkaFriendshipMessage {
    private String receiver;

    private String sender;
}
