package org.example.service;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.auth.JwtService;
import org.example.domain.*;
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
    public List<ExistingChatResponse> getExistingChats (HttpServletRequest request, String receiver, int page, int length){
        String userName = jwtService.extractUserName(request);
        Pageable pageable = PageRequest.of(page, length);
        List<P2pChat> chats = chatRepo.findChatByUser(userName, pageable);
        if (chats.isEmpty()) {
            log.info(STR."Вернулся пустой список чатов, пользователь: \{userName}");
            return Collections.emptyList();
        }
        List<ExistingChatResponse> response = convertChatsToResponses(chats, userName);
        response = filterChatsByReceiver(response, receiver);
        if (response.isEmpty()) {
            log.info(STR."Вернулся пустой список чатов после фильтрации по пользователю, пользователь: \{userName} \nПолучатель: \{receiver}");
            return Collections.emptyList();
        }
        log.info("Отправлены чаты: " + response);
        return response;
    }


    public List<ExistingChatResponse> convertChatsToResponses(List<P2pChat> chats, String userName) {
        return chats.stream()
                .map(chat -> new ExistingChatResponse(chat.getId(), Objects.equals(userName, chat.getUser1()) ? chat.getUser2() : chat.getUser1(), getLastMessage(chat)))
                .collect(Collectors.toList());
    }

    private ExistingChatResponse.LastMessage getLastMessage(P2pChat chat) {
        return new ExistingChatResponse.LastMessage(
                chat.getLastMessage().getSender(),
                chat.getLastMessage().getContent(),
                chat.getLastMessage().getTimestamp()
        );
    }

    private List<ExistingChatResponse> filterChatsByReceiver(List<ExistingChatResponse> chats, String receiver) {
        return chats.stream()
                .filter(chat -> chat.getUser().toLowerCase().contains(receiver.toLowerCase()))
                .collect(Collectors.toList());
    }

}
