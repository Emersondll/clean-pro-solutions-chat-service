package br.com.cleanprosolutions.chat.repository;

import br.com.cleanprosolutions.chat.document.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Repository for {@link ChatRoom} persistence.
 *
 * @author Emerson Lima
 * @since 1.0.0
 */
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    Optional<ChatRoom> findByContractId(String contractId);

    boolean existsByContractId(String contractId);
}
