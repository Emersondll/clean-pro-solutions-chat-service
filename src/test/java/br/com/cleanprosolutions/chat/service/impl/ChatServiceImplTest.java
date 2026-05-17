package br.com.cleanprosolutions.chat.service.impl;

import br.com.cleanprosolutions.chat.document.ChatMessage;
import br.com.cleanprosolutions.chat.document.ChatRoom;
import br.com.cleanprosolutions.chat.dto.ChatMessageRequest;
import br.com.cleanprosolutions.chat.dto.ChatMessageResponse;
import br.com.cleanprosolutions.chat.exception.ChatRoomNotFoundException;
import br.com.cleanprosolutions.chat.repository.ChatMessageRepository;
import br.com.cleanprosolutions.chat.repository.ChatRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ChatServiceImpl}.
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ChatRoomRepository roomRepository;

    @Mock
    private ChatMessageRepository messageRepository;

    @InjectMocks
    private ChatServiceImpl service;

    private ChatRoom room;

    @BeforeEach
    void setUp() {
        room = ChatRoom.builder()
                .id("room-1")
                .clientId("client-1")
                .contractorId("contractor-1")
                .contractId("contract-1")
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("shouldCreateRoomWhenContractHasNoExistingRoom")
    void shouldCreateRoomWhenContractHasNoExistingRoom() {
        when(roomRepository.findByContractId("contract-1")).thenReturn(Optional.empty());
        when(roomRepository.save(any(ChatRoom.class))).thenReturn(room);

        final String roomId = service.getOrCreateRoom("client-1", "contractor-1", "contract-1");

        assertThat(roomId).isEqualTo("room-1");
        verify(roomRepository).save(any(ChatRoom.class));
    }

    @Test
    @DisplayName("shouldReturnExistingRoomIdWhenContractAlreadyHasRoom")
    void shouldReturnExistingRoomIdWhenContractAlreadyHasRoom() {
        when(roomRepository.findByContractId("contract-1")).thenReturn(Optional.of(room));

        final String roomId = service.getOrCreateRoom("client-1", "contractor-1", "contract-1");

        assertThat(roomId).isEqualTo("room-1");
    }

    @Test
    @DisplayName("shouldReturnSseEmitterOnSubscribe")
    void shouldReturnSseEmitterOnSubscribe() {
        when(roomRepository.existsById("room-1")).thenReturn(true);

        final SseEmitter emitter = service.subscribe("room-1");

        assertThat(emitter).isNotNull();
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenSubscribingToNonExistentRoom")
    void shouldThrowExceptionWhenSubscribingToNonExistentRoom() {
        when(roomRepository.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> service.subscribe("missing"))
                .isInstanceOf(ChatRoomNotFoundException.class);
    }

    @Test
    @DisplayName("shouldSaveMessageAndReturnResponse")
    void shouldSaveMessageAndReturnResponse() {
        final ChatMessageRequest request = new ChatMessageRequest("client-1", "Hello!");
        final ChatMessage saved = ChatMessage.builder()
                .id("msg-1")
                .roomId("room-1")
                .senderId("client-1")
                .content("Hello!")
                .sentAt(Instant.now())
                .build();

        when(roomRepository.existsById("room-1")).thenReturn(true);
        when(messageRepository.save(any(ChatMessage.class))).thenReturn(saved);

        final ChatMessageResponse result = service.sendMessage("room-1", request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("msg-1");
        assertThat(result.content()).isEqualTo("Hello!");
        assertThat(result.senderId()).isEqualTo("client-1");
        verify(messageRepository).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenSendingToNonExistentRoom")
    void shouldThrowExceptionWhenSendingToNonExistentRoom() {
        when(roomRepository.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> service.sendMessage("missing", new ChatMessageRequest("x", "hi")))
                .isInstanceOf(ChatRoomNotFoundException.class);
    }

    @Test
    @DisplayName("shouldReturnMessageHistoryOrderedBySentAt")
    void shouldReturnMessageHistoryOrderedBySentAt() {
        final Instant t1 = Instant.now().minusSeconds(60);
        final Instant t2 = Instant.now();
        final ChatMessage m1 = ChatMessage.builder().id("m1").roomId("room-1").senderId("s1").content("First").sentAt(t1).build();
        final ChatMessage m2 = ChatMessage.builder().id("m2").roomId("room-1").senderId("s2").content("Second").sentAt(t2).build();

        when(roomRepository.existsById("room-1")).thenReturn(true);
        when(messageRepository.findByRoomIdOrderBySentAtAsc("room-1")).thenReturn(List.of(m1, m2));

        final List<ChatMessageResponse> history = service.getHistory("room-1");

        assertThat(history).hasSize(2);
        assertThat(history.get(0).content()).isEqualTo("First");
        assertThat(history.get(1).content()).isEqualTo("Second");
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenGettingHistoryForNonExistentRoom")
    void shouldThrowExceptionWhenGettingHistoryForNonExistentRoom() {
        when(roomRepository.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> service.getHistory("missing"))
                .isInstanceOf(ChatRoomNotFoundException.class);
    }
}
