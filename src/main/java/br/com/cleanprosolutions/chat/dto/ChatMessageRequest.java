package br.com.cleanprosolutions.chat.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Inbound request for sending a chat message.
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
public record ChatMessageRequest(

        @NotBlank(message = "senderId is required")
        String senderId,

        @NotBlank(message = "content cannot be blank")
        String content
) {}
