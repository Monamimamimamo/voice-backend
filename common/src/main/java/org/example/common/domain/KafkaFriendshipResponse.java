package org.example.common.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KafkaFriendshipResponse {
    private String status;

    @JsonCreator
    public KafkaFriendshipResponse(@JsonProperty("status") String status) {
        this.status = status;
    }
}
