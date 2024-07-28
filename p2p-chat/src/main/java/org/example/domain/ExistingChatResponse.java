package org.example.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExistingChatResponse {

    private UUID id;

    private String user;

    private LastMessage lastMessage;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LastMessage {
        private String sender;
        private String content;
        private String timestamp;
    }
}
