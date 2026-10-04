package com.candidjava.roles.config;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

/**
 * Renders the fully resolved prompt that Spring AI hands to the ChatModel.
 * <p>
 * By the time an advisor runs, the {@code defaultSystem} text and the
 * {@code defaultOptions} from the {@code ChatClient.Builder} have already been
 * merged into the {@link Prompt}, so this is what OpenAI actually receives.
 */
final class PromptLogger {

    private PromptLogger() {
    }

    static String formatRequest(ChatClientRequest request) {
        Prompt prompt = request.prompt();

        StringBuilder log = new StringBuilder("\n---- PROMPT SENT TO LLM ----");
        log.append("\noptions: ").append(prompt.getOptions());
        for (Message message : prompt.getInstructions()) {
            log.append("\n[").append(message.getMessageType().getValue()).append("]");
            log.append("\n").append(message.getText());
        }
        return log.append("\n---- END PROMPT ----").toString();
    }

    static String formatResponse(ChatResponse response) {
        return "\n---- RESPONSE FROM LLM ----\n"
                + response.getResult().getOutput().getText()
                + "\n---- END RESPONSE ----";
    }
}
