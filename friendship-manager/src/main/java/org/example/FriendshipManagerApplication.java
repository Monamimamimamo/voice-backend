package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableWebSocket
public class FriendshipManagerApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(FriendshipManagerApplication.class, args);
    }
}