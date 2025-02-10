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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
        List<ChatDocument> messages = chatService.getMessagesBefore(projectId, before);

        List<ChatDto> messageDtos = messages.stream().map(ChatDto::new).collect(Collectors.toList());

        return ResponseEntity.ok(messageDtos);
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
