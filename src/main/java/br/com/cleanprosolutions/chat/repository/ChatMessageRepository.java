package br.com.cleanprosolutions.chat.repository;

import br.com.cleanprosolutions.chat.document.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository for {@link ChatMessage} persistence.
 *
 * @author Emerson Lima
 * @since 1.0.0
 */
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    List<ChatMessage> findByRoomIdOrderBySentAtAsc(String roomId);
}
