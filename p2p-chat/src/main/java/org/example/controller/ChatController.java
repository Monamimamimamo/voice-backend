package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.ExistingChatResponse;
import org.example.domain.Message;
import org.example.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;

//    @KafkaListener(topics = "friendship-request-topic", groupId = "group_id")
//    @SendTo("friendship-response-topic")
//    public FriendshipResponse getCurrencyData(ConsumerRecord<String, KafkaFriendshipRequest> record) {
//        FriendshipResponse result = new FriendshipResponse(true,"completed");
//        try {
//            KafkaFriendshipRequest request = record.value();
//            log.info(request.toString());
//            log.info("Возвращаем: " + result);
//            return result;
//        } catch (Exception e) {
//            log.error("Ошибка при обработке сообщения Kafka: {}", e.getMessage());
//        }
//        result.setDescription("not completed");
//        result.setResult(false);
//        log.info("Возвращаем: " + result);
//        return result;
//    }

    @GetMapping("/history/messages")
    @Operation(summary = "Получение истории сообщений двух пользователей", parameters = {@Parameter(in = ParameterIn.HEADER, name = HttpHeaders.AUTHORIZATION, required = true, description = "JWT Bearer токен пользователя")})
    public ResponseEntity<List<Message>> getMessagesHistory(HttpServletRequest request,
                                                            @Parameter(description = "ID получателя") @RequestParam(name = "receiverId", required = true) String receiverId,
                                                            @Parameter(description = "Страница пагинации") @RequestParam(name = "page", required = true) int page,
                                                            @Parameter(description = "Длина страницы пагинации") @RequestParam(name = "length", required = true) int length) {
        if (length <= 0) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(chatService.getMessagesHistory(request, receiverId, page, length), HttpStatus.OK);
    }

    @GetMapping("/find-existing-chats")
    @Operation(summary = "Получение существующих чатов пользователя с последним сообщением", parameters = {@Parameter(in = ParameterIn.HEADER, name = HttpHeaders.AUTHORIZATION, required = true, description = "JWT Bearer токен пользователя")})
    public ResponseEntity<List<ExistingChatResponse>> getExistingChats(HttpServletRequest request,
                                                                       @Parameter(description = "Страница пагинации") @RequestParam(name = "page", required = true) int page,
                                                                       @Parameter(description = "Длина страницы пагинации") @RequestParam(name = "length", required = true) int length,
                                                                       @Parameter(description = "Тот, кому мы писали") @RequestParam(name = "receiver", required = true) String receiver) {
        if (length <= 0) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(chatService.getExistingChats(request, receiver, page, length), HttpStatus.OK);
    }
}
