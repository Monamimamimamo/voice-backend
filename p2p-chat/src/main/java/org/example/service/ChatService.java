package org.example.service;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
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
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Transactional
    public List<P2pChat> getExistingChats (HttpServletRequest request, String receiver, int page, int length){
        String userName = jwtService.extractUserName(request);
        Pageable pageable = PageRequest.of(page, length);
        List<P2pChat> chats = chatRepo.findChatByUser(userName, pageable);
        if (Objects.equals(receiver, ""))
            chats = filterChatsByReceiver(chats, receiver);
        if (chats.isEmpty()) {
            log.info(STR."Вернулся пустой список чатов, пользователь: \{userName} \nПолучатель: \{receiver}");
            return Collections.emptyList();
        }
        log.info("Отправлены чаты: " + chats);
        return chats;
    }

    private List<P2pChat> filterChatsByReceiver(List<P2pChat> chats, String receiver) {
        return chats.stream()
                .filter(chat -> chat.getUser1().contains(receiver) || chat.getUser2().contains(receiver))
                .collect(Collectors.toList());
    }

}
