package com.candidjava.toolshelpdesk.config;

import com.candidjava.toolshelpdesk.advisor.LlmCallLoggingAdvisor;
import com.candidjava.toolshelpdesk.tools.HelpDeskTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;

@Configuration
public class HelpDeskChatClientConfig {

    private static final Logger log = LoggerFactory.getLogger(HelpDeskChatClientConfig.class);

    @Value("classpath:/promptTemplates/helpDeskSystemPromptTemplate.st")
    private Resource systemPromptTemplate;

    @Bean
    ChatClient chatClient(ChatClient.Builder chatClientBuilder, HelpDeskTools helpDeskTools) {
        log.info("Creating helpdesk ChatClient (system prompt + HelpDeskTools + LLM call logging)");
        return chatClientBuilder
                .defaultSystem(systemPromptTemplate)
                .defaultTools(helpDeskTools)
                .defaultAdvisors(List.of(new LlmCallLoggingAdvisor(), new SimpleLoggerAdvisor()))
                .build();
    }
}
