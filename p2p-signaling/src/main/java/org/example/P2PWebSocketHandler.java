package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

public class P2PWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Gson gson = new GsonBuilder().create();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Парсим JSON сообщение
        final JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);

        // Обрабатываем сообщение в зависимости от его типа
        switch (jsonMessage.get("type").getAsString()) {
            case "offer":
                // Пересылка предложения всем подключенным клиентам, кроме отправителя
                for (WebSocketSession clientSession : sessions.values()) {
                    if (!clientSession.getId().equals(session.getId())) {
                        clientSession.sendMessage(message);
                    }
                }
                break;
            case "answer":
                // Пересылка ответа всем подключенным клиентам, кроме отправителя
                for (WebSocketSession clientSession : sessions.values()) {
                    if (!clientSession.getId().equals(session.getId())) {
                        clientSession.sendMessage(message);
                    }
                }
                break;
            case "candidate":
                // Пересылка ICE кандидата всем подключенным клиентам, кроме отправителя
                for (WebSocketSession clientSession : sessions.values()) {
                    if (!clientSession.getId().equals(session.getId())) {
                        clientSession.sendMessage(message);
                    }
                }
                break;
            default:
                // Обработка неизвестных типов сообщений
                break;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
    }
}
