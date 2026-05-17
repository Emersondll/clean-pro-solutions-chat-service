package br.com.cleanprosolutions.chat.service.impl;

import br.com.cleanprosolutions.chat.document.ChatMessage;
import br.com.cleanprosolutions.chat.document.ChatRoom;
import br.com.cleanprosolutions.chat.dto.ChatMessageRequest;
import br.com.cleanprosolutions.chat.dto.ChatMessageResponse;
import br.com.cleanprosolutions.chat.exception.ChatRoomNotFoundException;
import br.com.cleanprosolutions.chat.repository.ChatMessageRepository;
import br.com.cleanprosolutions.chat.repository.ChatRoomRepository;
import br.com.cleanprosolutions.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementation of {@link ChatService}.
 *
 * <p>Real-time delivery is achieved via Server-Sent Events. An in-memory registry
 * ({@code emitters}) maps each room to its active SSE connections. When a message
 * is saved, all subscribers are notified immediately.</p>
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    private final ChatRoomRepository roomRepository;
    private final ChatMessageRepository messageRepository;

    /** Room ID → active SSE emitters for that room. */
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    @Override
    public String getOrCreateRoom(final String clientId, final String contractorId, final String contractId) {
        return roomRepository.findByContractId(contractId)
                .map(ChatRoom::getId)
                .orElseGet(() -> {
                    final ChatRoom room = ChatRoom.builder()
                            .clientId(clientId)
                            .contractorId(contractorId)
                            .contractId(contractId)
                            .build();
                    final String id = roomRepository.save(room).getId();
                    log.info("Created chat room {} for contract {}", id, contractId);
                    return id;
                });
    }

    @Override
    public SseEmitter subscribe(final String roomId) {
        assertRoomExists(roomId);

        final SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        final CopyOnWriteArrayList<SseEmitter> roomEmitters =
                emitters.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>());
        roomEmitters.add(emitter);

        final Runnable cleanup = () -> roomEmitters.remove(emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(ex -> cleanup.run());

        log.info("Client subscribed to room {}", roomId);
        return emitter;
    }

    @Override
    public ChatMessageResponse sendMessage(final String roomId, final ChatMessageRequest request) {
        assertRoomExists(roomId);

        final ChatMessage saved = messageRepository.save(ChatMessage.builder()
                .roomId(roomId)
                .senderId(request.senderId())
                .content(request.content())
                .build());

        final ChatMessageResponse response = toResponse(saved);
        broadcast(roomId, response);
        return response;
    }

    @Override
    public List<ChatMessageResponse> getHistory(final String roomId) {
        assertRoomExists(roomId);
        return messageRepository.findByRoomIdOrderBySentAtAsc(roomId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void assertRoomExists(final String roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new ChatRoomNotFoundException(roomId);
        }
    }

    private void broadcast(final String roomId, final ChatMessageResponse response) {
        final List<SseEmitter> roomEmitters = emitters.getOrDefault(roomId, new CopyOnWriteArrayList<>());
        final List<SseEmitter> dead = new java.util.ArrayList<>();

        for (final SseEmitter emitter : roomEmitters) {
            try {
                emitter.send(SseEmitter.event().name("message").data(response));
            } catch (IOException e) {
                log.warn("Failed to send SSE to subscriber in room {}, removing", roomId);
                dead.add(emitter);
            }
        }
        roomEmitters.removeAll(dead);
    }

    private ChatMessageResponse toResponse(final ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getRoomId(),
                message.getSenderId(),
                message.getContent(),
                message.getSentAt()
        );
    }
}
