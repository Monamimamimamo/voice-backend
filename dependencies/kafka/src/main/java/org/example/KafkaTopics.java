package org.example;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Getter
public class KafkaTopics {

    private final String friendshipRequestTopic = "friendship_request_topic";

    private final String friendshipResponseTopic = "friendship_response_topic";

    public String getChatRequestTopic(){return "chat_request_topic";}
    public String getChatResponseTopic(){return "chat_response_topic";}

    @Bean
    public List<String> responseTopics() {
        return Arrays.asList("chat_response_topic", friendshipResponseTopic);
    }
}
