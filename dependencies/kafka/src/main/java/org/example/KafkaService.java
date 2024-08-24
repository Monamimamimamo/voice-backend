package org.example;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@AllArgsConstructor
public class KafkaService{
    private final ReplyingKafkaTemplate<Object, Object, Object> friendshipReplyingKafkaTemplate;
    private final ReplyingKafkaTemplate<Object, Object, Object> chatReplyingKafkaTemplate;
    private final KafkaTemplate<Object, Object> kafkaTemplate;


    public Object friendshipSendAndReceive(Object map) throws ExecutionException, InterruptedException {
        ProducerRecord<Object, Object> record = new ProducerRecord<>("friendship_request_topic", map);
        log.info("В kafka отправлен объект: {} На топик: {}", record, "friendship_request_topic");
        RequestReplyFuture<Object, Object, Object> futureResponse = friendshipReplyingKafkaTemplate.sendAndReceive(record, Duration.ofSeconds(7));
        log.info("Из kafka получен объект: {}", futureResponse.get().value());
        return futureResponse.get().value();
    }

    public Object chatSendAndReceive(Object map) throws ExecutionException, InterruptedException {
        ProducerRecord<Object, Object> record = new ProducerRecord<>("chat_request_topic", map);
        log.info("В kafka отправлен объект: {} На топик: {}", record, "chat_request_topic");
        RequestReplyFuture<Object, Object, Object> futureResponse = chatReplyingKafkaTemplate.sendAndReceive(record, Duration.ofSeconds(7));
        log.info("Из kafka получен объект: {}", futureResponse.get().value());
        return futureResponse.get().value();
    }

    public void send(Object map, String sendTopic) throws ExecutionException, InterruptedException {
        log.info("Отправлен объект: {}", map);
        kafkaTemplate.send(sendTopic, map);
    }
}
