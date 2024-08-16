package org.example.common.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class KafkaService {
    private final ReplyingKafkaTemplate<String, KafkaFriendshipRequest, FriendshipResponse> replyingKafkaTemplate;

    @Autowired
    public KafkaService(ReplyingKafkaTemplate<String, KafkaFriendshipRequest, FriendshipResponse> replyingKafkaTemplate) {
        this.replyingKafkaTemplate = replyingKafkaTemplate;
    }

    public String sendKafkaMessage(String senderId, String receiverId, String type) throws ExecutionException, InterruptedException {
        KafkaFriendshipRequest request = new KafkaFriendshipRequest(senderId, receiverId,type);
        ProducerRecord<String, KafkaFriendshipRequest> record = new ProducerRecord<>("friendship-request-topic", request);
        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, "friendship-response-topic".getBytes()));
        log.info("Отправлен объект: " + record);
        RequestReplyFuture<String, KafkaFriendshipRequest, FriendshipResponse> futureResponse =  replyingKafkaTemplate.sendAndReceive(record, Duration.ofSeconds(20));
        log.info("Получен объект: " + futureResponse.get().value());
        FriendshipResponse response = futureResponse.get().value();
        return response.getDescription();
    }
}
