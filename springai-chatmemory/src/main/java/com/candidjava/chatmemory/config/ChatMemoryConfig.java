package com.candidjava.chatmemory.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChatMemoryConfig {

    private static final Logger log = LoggerFactory.getLogger(ChatMemoryConfig.class);

    /** Keep only the last N messages per conversation (short-term window). */
    public static final int MAX_MESSAGES = 10;

    /**
     * ChatMemory = short-term conversation store.
     * MessageWindowChatMemory limits history size.
     * JdbcChatMemoryRepository persists messages in H2 (DB-backed memory).
     */
    @Bean
    ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        log.info("Creating ChatMemory: MessageWindowChatMemory maxMessages={}, repository=JdbcChatMemoryRepository",
                MAX_MESSAGES);
        return MessageWindowChatMemory.builder()
                .maxMessages(MAX_MESSAGES)
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .build();
    }

    /**
     * ChatClient with:
     * - SimpleLoggerAdvisor (request/response logging)
     * - MessageChatMemoryAdvisor (loads/saves history for CONVERSATION_ID)
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        Advisor loggerAdvisor = new SimpleLoggerAdvisor();
        Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        log.info("Creating ChatClient with advisors: SimpleLoggerAdvisor + MessageChatMemoryAdvisor");
        return chatClientBuilder
                .defaultAdvisors(List.of(loggerAdvisor, memoryAdvisor))
                .build();
    }
}
