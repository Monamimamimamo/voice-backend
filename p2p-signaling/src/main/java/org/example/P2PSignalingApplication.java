package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableWebSocket
@ComponentScan
public class P2PSignalingApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(P2PSignalingApplication.class, args);
    }
}