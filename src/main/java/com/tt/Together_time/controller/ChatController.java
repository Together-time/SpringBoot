package com.tt.Together_time.controller;

import com.tt.Together_time.domain.dto.ChatDto;
import com.tt.Together_time.service.ChatService;
import com.tt.Together_time.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<List<ChatDto>> getMessages(
            @RequestParam Long projectId,
            @RequestParam long start,
            @RequestParam long end) {
        List<ChatDto> messages = chatService.getChatMessages(projectId, start, end);

        return ResponseEntity.ok(messages);
    }
    //안 읽은 메시지 개수
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @RequestParam String projectId) {
        String loggedInMember = memberService.getUserEmail();

        long count = chatService.getUnreadMessageCount(projectId, loggedInMember);
        return ResponseEntity.ok(count);
    }
}
