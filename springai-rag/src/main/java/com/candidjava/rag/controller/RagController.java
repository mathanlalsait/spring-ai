package com.candidjava.rag.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Separate endpoints so each RAG concept can be taught in isolation.
 *
 * <ul>
 *   <li>Lesson A — sentence docs</li>
 *   <li>Lesson B — HR txt file + chunking</li>
 *   <li>Lesson C — PDF + DOCX readers</li>
 *   <li>PII — post-retrieve masking</li>
 *   <li>Translate — query transformer (optional)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/rag")
public class RagController {

    private static final Logger log = LoggerFactory.getLogger(RagController.class);

    private final ChatClient lessonAChatClient;
    private final ChatClient lessonBChatClient;
    private final ChatClient lessonCChatClient;
    private final ChatClient piiChatClient;
    private final ChatClient translateChatClient;

    public RagController(
            @Qualifier("lessonAChatClient") ChatClient lessonAChatClient,
            @Qualifier("lessonBChatClient") ChatClient lessonBChatClient,
            @Qualifier("lessonCChatClient") ChatClient lessonCChatClient,
            @Qualifier("piiChatClient") ChatClient piiChatClient,
            @Qualifier("translateChatClient") ChatClient translateChatClient) {
        this.lessonAChatClient = lessonAChatClient;
        this.lessonBChatClient = lessonBChatClient;
        this.lessonCChatClient = lessonCChatClient;
        this.piiChatClient = piiChatClient;
        this.translateChatClient = translateChatClient;
    }

    /** Lesson A: simple sentences in VectorStore — no file, no chunking, no PII. */
    @GetMapping("/lesson-a")
    public ResponseEntity<String> lessonA(@RequestParam("message") String message) {
        return ask("LESSON-A", lessonAChatClient, message);
    }

    /** Lesson B: hr-policy.txt → TextReader → TokenTextSplitter → retrieve. */
    @GetMapping("/lesson-b")
    public ResponseEntity<String> lessonB(@RequestParam("message") String message) {
        return ask("LESSON-B", lessonBChatClient, message);
    }

    /** Lesson C: product-faq.pdf + employee-handbook.docx → PDF/Tika readers → retrieve. */
    @GetMapping("/lesson-c")
    public ResponseEntity<String> lessonC(@RequestParam("message") String message) {
        return ask("LESSON-C", lessonCChatClient, message);
    }

    /** PII: same HR corpus as Lesson B, but emails/phones masked before the prompt. */
    @GetMapping("/pii")
    public ResponseEntity<String> pii(@RequestParam("message") String message) {
        return ask("PII", piiChatClient, message);
    }

    /** Optional: TranslationQueryTransformer then retrieve from HR policy. */
    @GetMapping("/translate")
    public ResponseEntity<String> translate(@RequestParam("message") String message) {
        return ask("TRANSLATE", translateChatClient, message);
    }

    private ResponseEntity<String> ask(String label, ChatClient client, String message) {
        log.info("=== RAG {} REQUEST === message='{}'", label, message);
        String answer = client.prompt()
                .user(message)
                .call()
                .content();
        log.info("=== RAG {} RESPONSE === '{}'", label, truncate(answer));
        return ResponseEntity.ok(answer);
    }

    private static String truncate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 160 ? text : text.substring(0, 160) + "...";
    }
}
