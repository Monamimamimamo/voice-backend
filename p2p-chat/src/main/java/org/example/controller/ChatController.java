package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.common.auth.JwtService;
import org.example.common.kafka.KafkaFriendshipRequest;
import org.example.common.kafka.KafkaFriendshipResponse;
import org.example.domain.Message;
import org.example.domain.MessageRepo;
import org.example.domain.P2pChat;
import org.example.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;

    @GetMapping("/history/messages")
    @Operation(summary = "Получение истории сообщений двух пользователей", parameters = {@Parameter(in = ParameterIn.HEADER, name = HttpHeaders.AUTHORIZATION, required = true, description = "JWT Bearer токен пользователя")})
    public ResponseEntity<List<Message>> getMessagesHistory(HttpServletRequest request,
                                                            @Parameter(description = "ID получателя") @RequestParam(name = "receiverId", required = true) String receiverId,
                                                            @Parameter(description = "Страница пагинации") @RequestParam(name = "page", required = true) int page,
                                                            @Parameter(description = "Длина страницы пагинация") @RequestParam(name = "length", required = true) int length) {
        if (length <= 0) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(chatService.getMessagesHistory(request, receiverId, page, length), HttpStatus.OK);
    }

    @GetMapping("/existing")
    @Operation(summary = "Получение существующих чатов пользователя с последним сообщением", parameters = {@Parameter(in = ParameterIn.HEADER, name = HttpHeaders.AUTHORIZATION, required = true, description = "JWT Bearer токен пользователя")})
    public ResponseEntity<List<P2pChat>> getExistingChats(HttpServletRequest request,
                                                          @Parameter(description = "Страница пагинации") @RequestParam(name = "page", required = true) int page,
                                                          @Parameter(description = "Длина страницы пагинация") @RequestParam(name = "length", required = true) int length) {
        if (length <= 0) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(chatService.getExistingChats(request, page, length), HttpStatus.OK);
    }
}
