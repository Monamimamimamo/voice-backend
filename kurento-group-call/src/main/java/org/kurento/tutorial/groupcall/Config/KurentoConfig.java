package org.kurento.tutorial.groupcall.Config;

import org.kurento.client.KurentoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KurentoConfig {
    @Bean
    public KurentoClient kurentoClient() {
        return KurentoClient.create("ws://45.141.76.83:8888/kurento");
    }
}
