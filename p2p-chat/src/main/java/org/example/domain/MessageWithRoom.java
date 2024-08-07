package org.example.domain;

import lombok.Data;

@Data
public class MessageWithRoom{
    String roomID;

    Message message;
}
