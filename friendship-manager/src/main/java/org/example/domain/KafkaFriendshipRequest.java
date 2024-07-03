package org.example.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class KafkaFriendshipRequest {
    private String type;
    private String receiver;
    private String sender;
}


