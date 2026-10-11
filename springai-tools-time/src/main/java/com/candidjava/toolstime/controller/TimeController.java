package com.candidjava.toolstime.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tools")
public class TimeController {

    private static final Logger log = LoggerFactory.getLogger(TimeController.class);

    private final ChatClient chatClient;

    public TimeController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/local-time")
    public ResponseEntity<String> localTime(@RequestParam("message") String message) {
        log.info("=== TOOLS-TIME REQUEST === message='{}'", message);
        String answer = chatClient.prompt()
                .user(message)
                .call()
                .content();
        log.info("=== TOOLS-TIME RESPONSE === '{}'", truncate(answer));
        return ResponseEntity.ok(answer);
    }

    private static String truncate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 160 ? text : text.substring(0, 160) + "...";
    }
}
