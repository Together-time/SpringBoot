package com.tt.Together_time.controller;

import com.tt.Together_time.domain.mongodb.ChatDocument;
import com.tt.Together_time.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/messages")
    public ResponseEntity<List<ChatDocument>> getMessages(
            @RequestParam Long projectId,
            @RequestParam long start,
            @RequestParam long end) {
        List<ChatDocument> messages = chatService.getChatMessages(projectId, start, end);

        return ResponseEntity.ok(messages);
    }
}
