package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;


@RestController
public class P2PWebSocketHandler {

    @MessageMapping("/signaling/{senderId}/{receiverId}")
    @SendTo("/topic/signaling/{senderId}/{receiverId}")
    public String handleChat(String message, @DestinationVariable("senderId") String senderId, @DestinationVariable("receiverId") String receiverId) throws IOException, ParseException {
        JSONObject parsedJson = (JSONObject) new JSONParser().parse(message);
        System.out.println(message);
        Object type = parsedJson.get("type");
        if (type.equals("offer")) {
            return message;
        } else if (type.equals("answer")) {
            return message;
        } else if (type.equals("candidate")) {
            return message;
        }
        return message;
    }
}
