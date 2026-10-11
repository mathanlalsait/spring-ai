package com.candidjava.toolshelpdesk.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.List;

/**
 * Training advisor: log clearly BEFORE and AFTER each ChatClient LLM call.
 */
public class LlmCallLoggingAdvisor implements CallAdvisor {

    private static final Logger log = LoggerFactory.getLogger(LlmCallLoggingAdvisor.class);

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        log.info(">>> LLM CALL START — messages={}", summarizeMessages(request));
        long start = System.currentTimeMillis();
        ChatClientResponse response = chain.nextCall(request);
        long elapsed = System.currentTimeMillis() - start;
        log.info("<<< LLM CALL END — elapsedMs={}, reply='{}'", elapsed, summarizeReply(response));
        return response;
    }

    private static String summarizeMessages(ChatClientRequest request) {
        if (request == null || request.prompt() == null || request.prompt().getInstructions() == null) {
            return "[]";
        }
        List<Message> messages = request.prompt().getInstructions();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < messages.size(); i++) {
            Message m = messages.get(i);
            if (i > 0) {
                sb.append(", ");
            }
            String text = m.getText() != null ? m.getText() : "";
            if (text.length() > 80) {
                text = text.substring(0, 80) + "...";
            }
            sb.append(m.getMessageType()).append('=').append(text);
        }
        sb.append(']');
        return sb.toString();
    }

    private static String summarizeReply(ChatClientResponse response) {
        ChatResponse chatResponse = response != null ? response.chatResponse() : null;
        if (chatResponse == null || chatResponse.getResult() == null) {
            return "";
        }
        Generation generation = chatResponse.getResult();
        String text = generation.getOutput() != null ? generation.getOutput().getText() : "";
        if (text == null) {
            text = "";
        }
        boolean toolCalls = generation.getOutput() != null
                && generation.getOutput().getToolCalls() != null
                && !generation.getOutput().getToolCalls().isEmpty();
        if (toolCalls) {
            return "toolCalls=" + generation.getOutput().getToolCalls().size() + " text='" + truncate(text) + "'";
        }
        return truncate(text);
    }

    private static String truncate(String text) {
        return text.length() <= 120 ? text : text.substring(0, 120) + "...";
    }

    @Override
    public String getName() {
        return "LlmCallLoggingAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
