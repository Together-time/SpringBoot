package com.tt.Together_time.controller;

import com.tt.Together_time.domain.dto.ChatDto;
import com.tt.Together_time.service.ChatService;
import com.tt.Together_time.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<List<ChatDto>> getMessagesBefore(
            @RequestParam Long projectId,
            @RequestParam LocalDateTime before) {
        List<ChatDto> messages = chatService.getMessagesBefore(projectId, before).stream()
                .map(ChatDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(messages);
    }
    //안 읽은 메시지 개수
    @GetMapping("/unread")
    public ResponseEntity<Long> getUnreadCount(
            @RequestParam Long projectId) {
        String loggedInMember = memberService.getUserEmail();
        long count = chatService.getUnreadMessageCount(projectId, loggedInMember);

        return ResponseEntity.ok(count);
    }
}
