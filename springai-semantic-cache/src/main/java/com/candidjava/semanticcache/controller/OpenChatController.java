package com.candidjava.semanticcache.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OpenChatController {

    private static final Logger log = LoggerFactory.getLogger(OpenChatController.class);

    private final ChatClient chatClient;

    public OpenChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/open-chat")
    public String chat(@RequestParam("message") String message) {
        long start = System.currentTimeMillis();
        log.info("=== SEMANTIC CACHE REQUEST === message='{}'", message);
        String reply = chatClient.prompt()
                .user(message)
                .call()
                .content();
        long elapsed = System.currentTimeMillis() - start;
        log.info("=== SEMANTIC CACHE RESPONSE === elapsedMs={}, reply='{}'", elapsed, truncate(reply));
        return reply;
    }

    private static String truncate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 160 ? text : text.substring(0, 160) + "...";
    }
}
