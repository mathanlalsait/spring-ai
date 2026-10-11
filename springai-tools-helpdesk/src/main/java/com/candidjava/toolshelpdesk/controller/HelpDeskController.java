package com.candidjava.toolshelpdesk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/tools")
public class HelpDeskController {

    private static final Logger log = LoggerFactory.getLogger(HelpDeskController.class);

    private final ChatClient chatClient;

    public HelpDeskController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/help-desk")
    public ResponseEntity<String> helpDesk(
            @RequestHeader("username") String username,
            @RequestParam("message") String message) {
        log.info("=== HELPDESK REQUEST === user='{}' message='{}'", username, message);
        String answer = chatClient.prompt()
                .user(message)
                .toolContext(Map.of("username", username))
                .call()
                .content();
        log.info("=== HELPDESK RESPONSE === '{}'", truncate(answer));
        return ResponseEntity.ok(answer);
    }

    private static String truncate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 160 ? text : text.substring(0, 160) + "...";
    }
}
