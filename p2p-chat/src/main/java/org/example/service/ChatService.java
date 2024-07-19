package org.example.service;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.auth.JwtService;
import org.example.domain.Message;
import org.example.domain.MessageRepo;
import org.example.domain.P2pChat;
import org.example.domain.P2pChatRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ChatService {

    private final MessageRepo messageRepo;
    private final JwtService jwtService;
    private final P2pChatRepository chatRepo;

    public List<Message> getMessagesHistory(HttpServletRequest request, String receiver, int page, int length){
            String userName = jwtService.extractUserName(request);
            Pageable pageable = PageRequest.of(page, length);
            List<Message> messages = messageRepo.findMessagesBySenderOrReceiver(userName, receiver, pageable);

            if (messages.isEmpty()) {
                log.info(STR."Вернулся пустой список сообщений, пользователи: \{userName} и \{receiver}");
                return Collections.emptyList();
            }
            log.info("Отправлено: " + messages);
            return messages;
    }

    public List<P2pChat> getExistingChats (HttpServletRequest request, int page, int length){
        String userName = jwtService.extractUserName(request);
        Pageable pageable = PageRequest.of(page, length);
        List<P2pChat> chats = chatRepo.findChatByUser(userName, pageable);
        if (chats.isEmpty()) {
            log.info(STR."Вернулся пустой список чатов, пользователь: \{userName}");
            return Collections.emptyList();
        }
        log.info("Отправлены чаты: " + chats);
        return chats;
    }

}
