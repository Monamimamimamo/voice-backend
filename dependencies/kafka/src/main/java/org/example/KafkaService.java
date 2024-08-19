package org.example;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final ReplyingKafkaTemplate<Object, Object, Object> replyingKafkaTemplate;


    public Object sendAndReceive(Object map, String sendTopic, String replyTopic) throws ExecutionException, InterruptedException {
        ProducerRecord<Object, Object> record = new ProducerRecord<>(sendTopic, map);
        record.headers().add(new RecordHeader(KafkaHeaders.REPLY_TOPIC, replyTopic.getBytes()));
        log.info(STR."В kafka отправлен объект: \{record}\nНа топик: \{sendTopic}");
        RequestReplyFuture<Object, Object, Object> futureResponse =  replyingKafkaTemplate.sendAndReceive(record, Duration.ofSeconds(7));
        log.info(STR."Из kafka получен объект: \{futureResponse.get().value()}\nИз топика: \{replyTopic}");
        return futureResponse.get().value();
    }

    public void send(Object map, String sendTopic) throws ExecutionException, InterruptedException {
        log.info(STR."Отправлен объект: \{map}");
        replyingKafkaTemplate.send(sendTopic, map);
    }
}
