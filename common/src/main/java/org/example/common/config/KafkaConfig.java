package org.example.common.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.common.domain.KafkaFriendshipRequest;
import org.example.common.domain.KafkaFriendshipResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig  {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;



    @Bean
    public ProducerFactory<String, KafkaFriendshipRequest> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "group_id");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, KafkaFriendshipRequest> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }


    @Bean
    public ReplyingKafkaTemplate<String, KafkaFriendshipRequest, KafkaFriendshipResponse> replyingKafkaTemplate(
            ProducerFactory<String, KafkaFriendshipRequest> pf,
            ConcurrentKafkaListenerContainerFactory<String, KafkaFriendshipResponse> factory) {

        ConcurrentMessageListenerContainer<String, KafkaFriendshipResponse> repliesContainer = factory.createContainer("friendship-response-topic");
        repliesContainer.getContainerProperties().setGroupId("group_id");
        repliesContainer.setAutoStartup(false);
        return new ReplyingKafkaTemplate<>(pf, repliesContainer);
    }

        @Bean
        public ConsumerFactory<String, KafkaFriendshipResponse> consumerFactory() {
            Map<String, Object> props = new HashMap<>();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "group_id");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
            JsonDeserializer<KafkaFriendshipResponse> jsonDeserializer = new JsonDeserializer<>(KafkaFriendshipResponse.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, jsonDeserializer.getClass().getName());

            return new DefaultKafkaConsumerFactory<>(props);
        }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaFriendshipResponse> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, KafkaFriendshipResponse> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setReplyTemplate(kafkaTemplate());
        return factory;
    }
}


