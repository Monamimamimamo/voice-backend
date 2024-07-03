package org.example.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.FriendshipWebSocketService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
@AllArgsConstructor
@Slf4j
public class FriendShipWebSocketController {

    private final FriendshipWebSocketService friendshipWebSocketService;

    @MessageMapping("/friendship/{receiverId}")
    public JSONObject handleFriendshipMessage(String message, @DestinationVariable("receiverId") String receiverId) throws ParseException, IOException, ExecutionException, InterruptedException {
        JSONObject parsedJson = (JSONObject) new JSONParser().parse(message);
        String senderJwt = (String) parsedJson.get("sender");
        String status = (String) parsedJson.get("status");
        log.info("Принято сообщение: " + message);
        return friendshipWebSocketService.handleFriendshipMessage(receiverId, senderJwt, status);
    }
}