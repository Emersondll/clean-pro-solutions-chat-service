package br.com.cleanprosolutions.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Entry point for the Chat Service.
 *
 * <p>Provides real-time WebSocket/STOMP chat between clients and contractors,
 * with message persistence in MongoDB.</p>
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ChatServiceApplication {

    public static void main(final String[] args) {
        SpringApplication.run(ChatServiceApplication.class, args);
    }
}
