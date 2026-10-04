package com.candidjava.semanticcache.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.cache.semantic.SemanticCache;
import org.springframework.ai.chat.cache.semantic.SemanticCacheAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.cache.semantic.DefaultSemanticCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Semantic cache backed by Qdrant VectorStore only (no Redis client / no Redis runtime).
 * DefaultSemanticCache class lives in the redis-semantic-cache module but supports vectorStore(...).
 */
@Configuration
public class SemanticCacheConfig {

    private static final Logger log = LoggerFactory.getLogger(SemanticCacheConfig.class);

    @Bean
    SemanticCache semanticCache(VectorStore vectorStore, EmbeddingModel embeddingModel) {
        log.info("Creating SemanticCache with Qdrant VectorStore, similarityThreshold=0.8 (no Redis)");
        return DefaultSemanticCache.builder()
                .vectorStore(vectorStore)
                .embeddingModel(embeddingModel)
                .similarityThreshold(0.8)
                .build();
    }

    @Bean
    SemanticCacheAdvisor semanticCacheAdvisor(SemanticCache semanticCache) {
        log.info("Creating SemanticCacheAdvisor");
        return SemanticCacheAdvisor.builder().cache(semanticCache).build();
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder chatClientBuilder, SemanticCacheAdvisor semanticCacheAdvisor) {
        log.info("Creating ChatClient with SimpleLoggerAdvisor + SemanticCacheAdvisor");
        return chatClientBuilder
                .defaultAdvisors(List.of(new SimpleLoggerAdvisor(), semanticCacheAdvisor))
                .build();
    }
}
