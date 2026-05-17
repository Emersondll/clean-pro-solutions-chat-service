package br.com.cleanprosolutions.chat.dto;

import java.time.Instant;

/**
 * Outbound response representing a single chat message.
 *
 * @author Emerson Lima
 * @since 1.0.0
 */
public record ChatMessageResponse(
        String id,
        String roomId,
        String senderId,
        String content,
        Instant sentAt
) {}
