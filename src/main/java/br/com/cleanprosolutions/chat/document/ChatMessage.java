package br.com.cleanprosolutions.chat.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Represents a single message within a {@link ChatRoom}.
 *
 * <p>Messages are indexed by {@code roomId} to support efficient retrieval
 * of a conversation's history.</p>
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
public class ChatMessage {

    @Id
    private String id;

    @Indexed
    private String roomId;

    private String senderId;

    private String content;

    @Builder.Default
    private Instant sentAt = Instant.now();
}
