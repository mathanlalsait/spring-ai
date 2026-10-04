package com.candidjava.ragwebsearch.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rag")
public class WebSearchRagController {

    private static final Logger log = LoggerFactory.getLogger(WebSearchRagController.class);

    private final ChatClient chatClient;

    public WebSearchRagController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/web-search/chat")
    public ResponseEntity<String> webSearchChat(@RequestParam("message") String message) {
        log.info("=== WEB-SEARCH RAG REQUEST === message='{}'", message);
        String answer = chatClient.prompt()
                .user(message)
                .call()
                .content();
        log.info("=== WEB-SEARCH RAG RESPONSE === '{}'", truncate(answer));
        return ResponseEntity.ok(answer);
    }

    private static String truncate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 160 ? text : text.substring(0, 160) + "...";
    }
}
