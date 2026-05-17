package br.com.cleanprosolutions.chat.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Represents a chat room between a client and a contractor, scoped to a contract.
 *
 * <p>A room is uniquely identified by the triplet (clientId, contractorId, contractId).
 * Each active contract gets its own isolated conversation channel.</p>
 *
 * @author Emerson Lima
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_rooms")
public class ChatRoom {

    @Id
    private String id;

    private String clientId;

    private String contractorId;

    private String contractId;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
