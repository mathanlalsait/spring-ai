package com.candidjava.chatmemory.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
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

    private final ChatClient chatMemoryChatClient;
    private final ChatClient noMemoryChatClient;
    private final ChatMemory chatMemory;

    public ChatMemoryController(
            @Qualifier("chatMemoryChatClient") ChatClient chatMemoryChatClient,
            @Qualifier("noMemoryChatClient") ChatClient noMemoryChatClient,
            ChatMemory chatMemory) {
        this.chatMemoryChatClient = chatMemoryChatClient;
        this.noMemoryChatClient = noMemoryChatClient;
        this.chatMemory = chatMemory;
    }

    /**
     * WITH memory — multi-turn chat keyed by conversation id (username header).
     * Same username → shared history; different username → separate history.
     */
    @GetMapping("/chat-memory")
    public ResponseEntity<String> chatWithMemory(@RequestHeader("username") String username,
            @RequestParam("message") String message) {

        int historyBefore = chatMemory.get(username).size();
        log.info("=== WITH MEMORY REQUEST === conversationId='{}', historySizeBefore={}, userMessage='{}'",
                username, historyBefore, message);

        String reply = chatMemoryChatClient.prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, username))
                .call()
                .content();

        int historyAfter = chatMemory.get(username).size();
        log.info("=== WITH MEMORY RESPONSE === conversationId='{}', historySizeAfter={}, assistantReply='{}'",
                username, historyAfter, truncate(reply));

        return ResponseEntity.ok(reply);
    }

    /**
     * WITHOUT memory — each call is independent (stateless).
     * Even if you send the same username, prior turns are NOT used.
     */
    @GetMapping("/chat-no-memory")
    public ResponseEntity<String> chatWithoutMemory(@RequestParam("message") String message) {
        log.info("=== WITHOUT MEMORY REQUEST === userMessage='{}' (no conversation id, no history loaded)",
                message);

        String reply = noMemoryChatClient.prompt()
                .user(message)
                .call()
                .content();

        log.info("=== WITHOUT MEMORY RESPONSE === assistantReply='{}'", truncate(reply));
        return ResponseEntity.ok(reply);
    }

    private static String truncate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 120 ? text : text.substring(0, 120) + "...";
    }
}
