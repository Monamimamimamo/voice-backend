package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SocketHandler extends TextWebSocketHandler {

    private Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        try {
            JsonNode json = new ObjectMapper().readTree(message.getPayload());
            JsonNode actionNode = json.get("action");
            if (actionNode == null) {
                return;
            }
            String action = actionNode.asText();
            String peerID;
            System.out.println(action);
            switch (action) {
                case "share-rooms":
                    shareRoomsInfo();
                case "join":
                    String roomId = json.get("room").asText();
                    Set<WebSocketSession> room = rooms.computeIfAbsent(roomId, k -> new HashSet<>());
                    if (room.contains(session)) {
                        session.sendMessage(new TextMessage("{\"error\":\"Already joined to " + roomId + "\"}"));
                    } else {
                        room.add(session);
                        notifyRoomMembers(roomId, session.getId());
                        shareRoomsInfo();
                    }
                    break;
                case "leave":
                    leaveRoom(json.get("room").asText(), session);
                case "disconnecting":
                    leaveRoom(json.get("room").asText(), session);
                    break;
                case "relay-sdp":
                    peerID = json.get("peerID").asText();
                    String sessionDescription = json.get("sessionDescription").asText();
                    relayMessage("session-description", session.getId(), peerID, sessionDescription);
                    break;
                case "relay-ice":
                    peerID = json.get("peerID").asText();
                    String iceCandidate = json.get("iceCandidate").asText();
                    relayMessage("ice-candidate", session.getId(), peerID, iceCandidate);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        shareRoomsInfo();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        rooms.values().forEach(room -> room.remove(session));
        // Уведомление о выходе из комнаты
        rooms.forEach((roomId, room) -> {
            if (room.contains(session)) {
                room.remove(session);
                notifyRoomMembers(roomId, session.getId());
            }
        });
        shareRoomsInfo();
    }

    private void leaveRoom(String roomId, WebSocketSession session) throws IOException {
        Set<WebSocketSession> room = rooms.get(roomId);
        String sessionID = session.getId();
        if (room != null) {
            room.remove(session);
            if (room.isEmpty()) {
                rooms.remove(roomId);
            }
            // Уведомление участников комнаты о выходе клиента
            room.forEach(participant -> {
                try {
                    participant.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(Map.of(
                            "action", "remove-peer",
                            "peerID", sessionID
                    ))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            try {
                session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(Map.of(
                        "action", "remove-peer",
                        "peerID", sessionID
                ))));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            shareRoomsInfo();
        }
    }


    private void shareRoomsInfo() {
        Set<String> roomIds = rooms.keySet();
        for (WebSocketSession session : sessions.values()) {
            try {
                session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(Map.of(
                        "action", "share-rooms",
                        "rooms", roomIds
                ))));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    private void notifyRoomMembers(String roomId, String newMemberId) {
        Set<WebSocketSession> room = rooms.get(roomId);
        if (room != null) {
            for (WebSocketSession session : room) {
                if (!session.getId().equals(newMemberId)) {
                    try {
                        session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(Map.of(
                                "action", "add-peer",
                                "peerID", newMemberId,
                                "createOffer", false
                        ))));
                        sessions.get(newMemberId).sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(Map.of(
                                "action", "add-peer",
                                "peerID", session.getId(),
                                "createOffer", false
                        ))));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private void relayMessage(String action, String fromId, String toId, String message) {
        WebSocketSession toSession = sessions.get(toId);
        if (toSession != null) {
            try {
                toSession.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(Map.of(
                        "action", action,
                        "from", fromId,
                        "message", message
                ))));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

