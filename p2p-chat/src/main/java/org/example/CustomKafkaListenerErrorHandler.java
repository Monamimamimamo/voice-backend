package org.example;

import lombok.AllArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CustomKafkaListenerErrorHandler implements KafkaListenerErrorHandler {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception) {

        if (exception.getCause() instanceof MessageConversionException) {
            String errorMessage = STR."Ошибка при обработке сообщения Kafka: \{exception.getMessage()}";
            simpMessagingTemplate.convertAndSend("/topic/error", errorMessage);
        }

        return null;
    }

    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception, Consumer<?, ?> consumer) {
        if (exception.getCause() instanceof MessageConversionException) {
            String errorMessage = STR."Ошибка при обработке сообщения Kafka: \{exception.getMessage()}";
            simpMessagingTemplate.convertAndSend("/topic/error", errorMessage);
        }

        return null;
    }

    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception, Consumer<?, ?> consumer, Acknowledgment ack) {
        if (exception.getCause() instanceof MessageConversionException) {
            String errorMessage = STR."Ошибка при обработке сообщения Kafka: \{exception.getMessage()}";
            simpMessagingTemplate.convertAndSend("/topic/error", errorMessage);
        }

        return null;
    }

}
