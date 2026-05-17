package br.com.cleanprosolutions.chat.exception;

/**
 * Thrown when a requested chat room does not exist.
 *
 * @author Emerson Lima
 * @since 1.0.0
 */
public class ChatRoomNotFoundException extends RuntimeException {

    public ChatRoomNotFoundException(final String roomId) {
        super("Chat room not found: " + roomId);
    }
}
