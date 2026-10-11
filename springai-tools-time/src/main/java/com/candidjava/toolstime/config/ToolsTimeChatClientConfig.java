package com.candidjava.toolstime.config;

import com.candidjava.toolstime.advisor.LlmCallLoggingAdvisor;
import com.candidjava.toolstime.tools.TimeTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.tool.execution.DefaultToolExecutionExceptionProcessor;
import org.springframework.ai.tool.execution.ToolExecutionException;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ToolsTimeChatClientConfig {

    private static final Logger log = LoggerFactory.getLogger(ToolsTimeChatClientConfig.class);

    /**
     * alwaysThrow=false → tool errors become messages for the model (it can recover).
     * alwaysThrow=true  → exception bubbles to the HTTP caller (500).
     * This bean also logs every tool failure for classroom demos.
     */
    @Bean
    ToolExecutionExceptionProcessor toolExecutionExceptionProcessor() {
        log.info("Registering ToolExecutionExceptionProcessor (alwaysThrow=false → send error to model)");
        DefaultToolExecutionExceptionProcessor delegate = new DefaultToolExecutionExceptionProcessor(false);
        return (ToolExecutionException exception) -> {
            Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
            log.error("!!! TOOL EXECUTION ERROR !!! message='{}' causeType={}",
                    exception.getMessage(),
                    cause.getClass().getSimpleName());
            String modelMessage = delegate.process(exception);
            log.info("Tool error message sent back to the LLM: '{}'", modelMessage);
            return modelMessage;
        };
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder chatClientBuilder, TimeTools timeTools) {
        log.info("Creating ChatClient with defaultTools(TimeTools) + before/after LLM logging");
        return chatClientBuilder
                .defaultSystem("""
                        You are a helpful assistant. Use the time tools when the user asks about the current time.
                        If a tool returns an error (for example an invalid time zone), explain the problem clearly
                        and ask the user for a valid IANA time zone such as Asia/Kolkata.
                        """)
                .defaultTools(timeTools)
                .defaultAdvisors(List.of(new LlmCallLoggingAdvisor(), new SimpleLoggerAdvisor()))
                .build();
    }
}
