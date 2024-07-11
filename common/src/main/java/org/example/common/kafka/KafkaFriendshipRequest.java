package org.example.common.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KafkaFriendshipRequest {
    private String sender;
    private String receiver;
    private String type;
}


