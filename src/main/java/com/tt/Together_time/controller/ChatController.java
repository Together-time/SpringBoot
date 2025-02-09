package com.tt.Together_time.controller;

import com.tt.Together_time.domain.dto.ChatDto;
import com.tt.Together_time.domain.mongodb.ChatDocument;
import com.tt.Together_time.repository.ChatMongoRepository;
import com.tt.Together_time.service.ChatService;
import com.tt.Together_time.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final MemberService memberService;

    private final ChatMongoRepository chatMongoRepository;

    @GetMapping
    public ResponseEntity<List<ChatDto>> getMessages(
            @RequestParam Long projectId,
            @RequestParam long start,
            @RequestParam long end) {
        List<ChatDto> messages = chatService.getChatMessages(projectId, start, end);

        return ResponseEntity.ok(messages);
    }
    //안 읽은 메시지 개수
    @GetMapping("/unread")
    public ResponseEntity<Long> getUnreadCount(
            @RequestParam Long projectId) {
        String loggedInMember = memberService.getUserEmail();

        log.info("loggedInMember {}", loggedInMember);

        long count = chatService.getUnreadMessageCount(projectId, loggedInMember);
        log.info("count {}", count);
        return ResponseEntity.ok(count);
    }
}
