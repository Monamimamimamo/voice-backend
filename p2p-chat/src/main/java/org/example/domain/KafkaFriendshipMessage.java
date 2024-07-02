package org.example.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
public class KafkaFriendshipMessage {
    private String type;
    private String receiver;
    private String sender;
}

