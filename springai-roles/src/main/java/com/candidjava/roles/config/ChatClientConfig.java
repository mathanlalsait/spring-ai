package com.candidjava.roles.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        var options = OpenAiChatOptions.builder()
                .model("gpt-4o-mini")
                .temperature(0.8);
        return chatClientBuilder
                .defaultOptions(options)
                .defaultSystem("""
                        You are an internal HR assistant. Your role is to help
                        employees with questions related to HR policies, such as
                        leave policies, working hours, benefits, and code of conduct.
                        If a user asks for help with anything outside of these topics,
                        kindly inform them that you can only assist with queries related to
                        HR policies.
                        """)
                .defaultAdvisors(SimpleLoggerAdvisor.builder()
                        .requestToString(PromptLogger::formatRequest)
                        .responseToString(PromptLogger::formatResponse)
                        .build())
                .build();
    }
}
