package com.candidjava.chatmemory.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/api")
public class ChatMemoryController {

    private static final Logger log = LoggerFactory.getLogger(ChatMemoryController.class);

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public ChatMemoryController(ChatClient chatClient, ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
    }

    /**
     * Multi-turn chat keyed by conversation id (username header).
     * Same username → shared history; different username → separate history.
     */
    @GetMapping("/chat-memory")
    public ResponseEntity<String> chatMemory(@RequestHeader("username") String username,
            @RequestParam("message") String message) {

        int historyBefore = chatMemory.get(username).size();
        log.info("=== CHAT MEMORY REQUEST === conversationId(username)='{}', historySizeBefore={}, userMessage='{}'",
                username, historyBefore, message);

        String reply = chatClient.prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, username))
                .call()
                .content();

        int historyAfter = chatMemory.get(username).size();
        log.info("=== CHAT MEMORY RESPONSE === conversationId='{}', historySizeAfter={}, assistantReply='{}'",
                username, historyAfter, truncate(reply));

        return ResponseEntity.ok(reply);
    }

    private static String truncate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 120 ? text : text.substring(0, 120) + "...";
    }
}
