package br.com.cleanprosolutions.chat.controller;

import br.com.cleanprosolutions.chat.dto.ChatMessageRequest;
import br.com.cleanprosolutions.chat.dto.ChatMessageResponse;
import br.com.cleanprosolutions.chat.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ChatController}.
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController controller;

    private ChatMessageResponse messageResponse;

    @BeforeEach
    void setUp() {
        messageResponse = new ChatMessageResponse(
                "msg-1", "room-1", "sender-1", "Hello!", Instant.now());
    }

    @Test
    @DisplayName("shouldGetOrCreateRoom")
    void shouldGetOrCreateRoom() {
        when(chatService.getOrCreateRoom("client-1", "contractor-1", "contract-1")).thenReturn("room-1");

        final ResponseEntity<String> result = controller.getOrCreateRoom("client-1", "contractor-1", "contract-1");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo("room-1");
    }

    @Test
    @DisplayName("shouldSubscribeToRoomStream")
    void shouldSubscribeToRoomStream() {
        final SseEmitter emitter = mock(SseEmitter.class);
        when(chatService.subscribe("room-1")).thenReturn(emitter);

        final SseEmitter result = controller.subscribe("room-1");

        assertThat(result).isEqualTo(emitter);
    }

    @Test
    @DisplayName("shouldSendMessage")
    void shouldSendMessage() {
        final ChatMessageRequest request = new ChatMessageRequest("sender-1", "Hello!");
        when(chatService.sendMessage(eq("room-1"), any(ChatMessageRequest.class))).thenReturn(messageResponse);

        final ResponseEntity<ChatMessageResponse> result = controller.sendMessage("room-1", request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(messageResponse);
    }

    @Test
    @DisplayName("shouldGetMessageHistory")
    void shouldGetMessageHistory() {
        when(chatService.getHistory("room-1")).thenReturn(List.of(messageResponse));

        final ResponseEntity<List<ChatMessageResponse>> result = controller.getHistory("room-1");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(1);
    }
}
