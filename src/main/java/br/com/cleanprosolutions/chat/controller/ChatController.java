package br.com.cleanprosolutions.chat.controller;

import br.com.cleanprosolutions.chat.dto.ChatMessageRequest;
import br.com.cleanprosolutions.chat.dto.ChatMessageResponse;
import br.com.cleanprosolutions.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * REST + SSE endpoints for real-time chat.
 *
 * <p>Clients subscribe to {@code GET /chat/{roomId}/stream} (SSE) to receive messages live,
 * and send messages via {@code POST /chat/{roomId}/messages}.</p>
 *
 * @author Clean Pro Solutions Team
 * @since 1.0.0
 */
@Tag(name = "Chat", description = "Real-time chat operations")
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "Get or create a chat room for a contract")
    @PostMapping("/rooms")
    public ResponseEntity<String> getOrCreateRoom(
            @RequestParam final String clientId,
            @RequestParam final String contractorId,
            @RequestParam final String contractId) {
        final String roomId = chatService.getOrCreateRoom(clientId, contractorId, contractId);
        return ResponseEntity.ok(roomId);
    }

    @Operation(summary = "Subscribe to live messages in a room (SSE)")
    @GetMapping(value = "/{roomId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable final String roomId) {
        return chatService.subscribe(roomId);
    }

    @Operation(summary = "Send a message to a room")
    @PostMapping("/{roomId}/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @PathVariable final String roomId,
            @Valid @RequestBody final ChatMessageRequest request) {
        return ResponseEntity.ok(chatService.sendMessage(roomId, request));
    }

    @Operation(summary = "Retrieve message history for a room")
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getHistory(@PathVariable final String roomId) {
        return ResponseEntity.ok(chatService.getHistory(roomId));
    }
}
