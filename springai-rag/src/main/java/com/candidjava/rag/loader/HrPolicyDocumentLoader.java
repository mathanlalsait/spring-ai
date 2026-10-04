package com.candidjava.rag.loader;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Lesson B / PII: load a policy text file, split into chunks, store embeddings.
 * Tagged with metadata source=hr-policy so policy demos stay isolated from Lesson A.
 */
@Component
public class HrPolicyDocumentLoader {

    public static final String SOURCE = "hr-policy";

    private static final Logger log = LoggerFactory.getLogger(HrPolicyDocumentLoader.class);

    private final VectorStore vectorStore;

    @Value("classpath:hr-policy.txt")
    private Resource policyFile;

    public HrPolicyDocumentLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void load() {
        TextReader reader = new TextReader(policyFile);
        List<Document> docs = reader.get();
        TextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(120)
                .withMaxNumChunks(50)
                .build();
        List<Document> chunks = splitter.split(docs).stream()
                .map(chunk -> chunk.mutate().metadata("source", SOURCE).build())
                .toList();
        vectorStore.add(chunks);
        log.info("Loaded HR policy into VectorStore as {} chunks (Lesson B/PII, source={})", chunks.size(), SOURCE);
    }
}
