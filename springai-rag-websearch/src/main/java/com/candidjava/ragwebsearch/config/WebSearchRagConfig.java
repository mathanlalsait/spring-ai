package com.candidjava.ragwebsearch.config;

import com.candidjava.ragwebsearch.retriever.WebSearchDocumentRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
public class WebSearchRagConfig {

    private static final Logger log = LoggerFactory.getLogger(WebSearchRagConfig.class);

    @Bean
    ChatClient chatClient(ChatClient.Builder chatClientBuilder, RestClient.Builder restClientBuilder) {
        log.info("Creating web-search RAG ChatClient (RetrievalAugmentationAdvisor + Tavily DocumentRetriever)");
        var webSearchAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(WebSearchDocumentRetriever.builder()
                        .restClientBuilder(restClientBuilder)
                        .maxResults(5)
                        .build())
                .build();
        return chatClientBuilder
                .defaultAdvisors(List.of(new SimpleLoggerAdvisor(), webSearchAdvisor))
                .build();
    }
}
