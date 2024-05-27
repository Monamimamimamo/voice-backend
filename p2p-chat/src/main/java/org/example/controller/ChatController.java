package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.domain.KafkaFriendshipMessage;
import org.springframework.http.HttpHeaders;
import org.example.domain.Message;
import org.example.domain.MessageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@Slf4j
public class ChatController {

    @Autowired
    private MessageRepo messageRepo;

    @KafkaListener(topics = "friendship-topic", errorHandler = "customKafkaListenerErrorHandler")
    public void getCurrencyData(ConsumerRecord<String, KafkaFriendshipMessage> record) {
        try {
            KafkaFriendshipMessage exchange = record.value();
            log.info(exchange.toString());
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения Kafka: {}", e.getMessage());
        }
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/chat/history/messages")
    public ResponseEntity<List<Message>> getMessages(HttpServletRequest request,
                                                     @RequestParam(name = "senderId", required = true) String senderId,
                                                     @RequestParam(name = "receiverId", required = true) String receiverId,
                                                     @RequestParam(name = "page", required = true) Integer page,
                                                     @RequestParam(name = "length", required = true) int length) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Unauthorized access
        }
//        String token = authHeader.substring(7);
//        Claims claims = Jwts.parser()
//                .setSigningKey("FBF0E5C5-056A-4DE9-A9B4-CAC04513C5D8".getBytes())
//                .parseClaimsJws(token)
//                .getBody();
//        String userName = claims.get("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress", String.class);
//
//        if (Objects.equals(senderId, userName)) {
//            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
//        }
        if (length <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Pageable pageable = PageRequest.of(page, length);

        List<Message> messages = messageRepo.findMessagesBySenderOrReceiver(senderId, receiverId, pageable);
        if (messages.isEmpty()) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        }

        return new ResponseEntity<>(messages, HttpStatus.OK);
    }
}
