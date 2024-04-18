package org.example;

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
            String action = json.get("action").asText();
            switch (action) {
                case "join":
                    String roomId = json.get("room").asText();
                    rooms.computeIfAbsent(roomId, k -> new HashSet<>()).add(session);
                    shareRoomsInfo();
                    notifyRoomMembers(roomId, session.getId());
                    break;
                case "leave":
                    leaveRoom(json.get("room").asText(), session.getId());
                    break;
                case "relaySDP":
                    relayMessage("sessionDescription", session.getId(), json.get("peerID").asText(), json.get("sessionDescription").asText());
                    break;
                case "relayICE":
                    relayMessage("iceCandidate", session.getId(), json.get("peerID").asText(), json.get("iceCandidate").asText());
                    break;
                // Добавьте здесь обработку других действий
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
        shareRoomsInfo();
    }

    private void shareRoomsInfo() {
        Set<String> roomIds = rooms.keySet();
        for (WebSocketSession session : sessions.values()) {
            try {
                session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(roomIds)));
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
                    relayMessage("addPeer", newMemberId, session.getId(), "New peer joined");
                }
            }
        }
    }

    private void leaveRoom(String roomId, String sessionId) {
        Set<WebSocketSession> room = rooms.get(roomId);
        if (room != null) {
            room.remove(sessions.get(sessionId));
            if (room.isEmpty()) {
                rooms.remove(roomId);
            }
            shareRoomsInfo();
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

