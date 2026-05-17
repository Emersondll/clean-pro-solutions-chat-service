package br.com.cleanprosolutions.chat.service;

import br.com.cleanprosolutions.chat.dto.ChatMessageRequest;
import br.com.cleanprosolutions.chat.dto.ChatMessageResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * Contract for chat room operations and real-time message delivery.
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
public interface ChatService {

    /**
     * Creates an SSE emitter for the caller to receive live messages in a room.
     *
     * @param roomId the chat room identifier
     * @return a long-lived {@link SseEmitter} that pushes new messages
     */
    SseEmitter subscribe(String roomId);

    /**
     * Persists a message and broadcasts it to all active subscribers in the room.
     *
     * @param roomId  the target chat room
     * @param request the message payload
     * @return the persisted message response
     */
    ChatMessageResponse sendMessage(String roomId, ChatMessageRequest request);

    /**
     * Returns the full message history for a room, ordered oldest-first.
     *
     * @param roomId the chat room identifier
     * @return ordered list of messages
     */
    List<ChatMessageResponse> getHistory(String roomId);

    /**
     * Finds or creates a chat room for the given contract.
     *
     * @param clientId     the client's identifier
     * @param contractorId the contractor's identifier
     * @param contractId   the associated contract identifier
     * @return the room identifier
     */
    String getOrCreateRoom(String clientId, String contractorId, String contractId);
}
