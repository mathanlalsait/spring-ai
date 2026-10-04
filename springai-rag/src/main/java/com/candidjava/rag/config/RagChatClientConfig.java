package com.candidjava.rag.config;

import com.candidjava.rag.loader.DocumentFormatsLoader;
import com.candidjava.rag.loader.HrPolicyDocumentLoader;
import com.candidjava.rag.loader.SentenceDataLoader;
import com.candidjava.rag.postprocess.PiiMaskingDocumentPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * One ChatClient per teaching concept so demos stay isolated.
 */
@Configuration
public class RagChatClientConfig {

    private static final Logger log = LoggerFactory.getLogger(RagChatClientConfig.class);

    /** Lesson A: short sentence docs only — basic RAG, no PII / no translate. */
    @Bean
    @Qualifier("lessonAChatClient")
    ChatClient lessonAChatClient(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        log.info("Creating Lesson A ChatClient (source=sentences, plain RAG)");
        return chatClientBuilder.build().mutate()
                .defaultAdvisors(List.of(
                        new SimpleLoggerAdvisor(),
                        plainRagAdvisor(vectorStore, SentenceDataLoader.SOURCE)))
                .build();
    }

    /** Lesson B: HR policy chunks only — file + TokenTextSplitter RAG. */
    @Bean
    @Qualifier("lessonBChatClient")
    ChatClient lessonBChatClient(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        log.info("Creating Lesson B ChatClient (source=hr-policy, plain RAG)");
        return chatClientBuilder.build().mutate()
                .defaultAdvisors(List.of(
                        new SimpleLoggerAdvisor(),
                        plainRagAdvisor(vectorStore, HrPolicyDocumentLoader.SOURCE)))
                .build();
    }

    /** Lesson C: PDF (PagePdfDocumentReader) + DOCX (TikaDocumentReader). */
    @Bean
    @Qualifier("lessonCChatClient")
    ChatClient lessonCChatClient(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        log.info("Creating Lesson C ChatClient (source=doc-formats, PDF + DOCX)");
        return chatClientBuilder.build().mutate()
                .defaultAdvisors(List.of(
                        new SimpleLoggerAdvisor(),
                        plainRagAdvisor(vectorStore, DocumentFormatsLoader.SOURCE)))
                .build();
    }

    /** PII lesson: same HR policy corpus + mask email/phone after retrieve. */
    @Bean
    @Qualifier("piiChatClient")
    ChatClient piiChatClient(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        log.info("Creating PII ChatClient (source=hr-policy + PiiMaskingDocumentPostProcessor)");
        RetrievalAugmentationAdvisor advisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever(vectorStore, HrPolicyDocumentLoader.SOURCE))
                .documentPostProcessors(PiiMaskingDocumentPostProcessor.create())
                .build();
        return chatClientBuilder.build().mutate()
                .defaultAdvisors(List.of(new SimpleLoggerAdvisor(), advisor))
                .build();
    }

    /** Optional: query translation before retrieve (HR policy corpus). */
    @Bean
    @Qualifier("translateChatClient")
    ChatClient translateChatClient(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        log.info("Creating Translate ChatClient (TranslationQueryTransformer + hr-policy)");
        RetrievalAugmentationAdvisor advisor = RetrievalAugmentationAdvisor.builder()
                .queryTransformers(TranslationQueryTransformer.builder()
                        .chatClientBuilder(chatClientBuilder.clone())
                        .targetLanguage("english")
                        .build())
                .documentRetriever(retriever(vectorStore, HrPolicyDocumentLoader.SOURCE))
                .build();
        return chatClientBuilder.build().mutate()
                .defaultAdvisors(List.of(new SimpleLoggerAdvisor(), advisor))
                .build();
    }

    private static RetrievalAugmentationAdvisor plainRagAdvisor(VectorStore vectorStore, String source) {
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever(vectorStore, source))
                .build();
    }

    private static VectorStoreDocumentRetriever retriever(VectorStore vectorStore, String source) {
        return VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(3)
                .similarityThreshold(0.5)
                .filterExpression(new FilterExpressionBuilder().eq("source", source).build())
                .build();
    }
}
