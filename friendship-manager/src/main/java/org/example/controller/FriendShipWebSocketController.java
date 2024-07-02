package org.example.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.example.domain.FriendshipOffer;
import org.example.domain.FriendshipOfferRepo;
import org.example.domain.KafkaFriendshipMessage;
import org.example.service.FriendshipWebSocketService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@AllArgsConstructor
public class FriendShipWebSocketController {

    private final FriendshipWebSocketService friendshipWebSocketService;

    @MessageMapping("/friendship/{receiverId}")
    public JSONObject handleFriendshipMessage(String message, @DestinationVariable("receiverId") String receiverId) throws ParseException, IOException {
        JSONObject parsedJson = (JSONObject) new JSONParser().parse(message);
        String senderJwt = (String) parsedJson.get("sender");
        String status = (String) parsedJson.get("status");

        return friendshipWebSocketService.handleFriendshipMessage(receiverId, senderJwt, status);
    }

}