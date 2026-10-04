package com.candidjava.rag.loader;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Lesson A: load simple sentences into the vector store (easy first RAG demo).
 * Tagged with metadata source=sentences so Lesson A retrieval stays isolated.
 */
@Component
public class SentenceDataLoader {

    public static final String SOURCE = "sentences";

    private static final Logger log = LoggerFactory.getLogger(SentenceDataLoader.class);

    private final VectorStore vectorStore;

    public SentenceDataLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void load() {
        List<Document> documents = List.of(
                doc("Java is used for building scalable enterprise applications."),
                doc("Python is commonly used for machine learning and automation tasks."),
                doc("Docker packages applications into lightweight containers."),
                doc("Kubernetes automates container orchestration at scale."),
                doc("Redis is an in-memory data store used for caching."),
                doc("Kafka is a distributed event streaming platform."),
                doc("Compound interest increases investment returns over time."),
                doc("Photosynthesis is how plants convert sunlight into energy."),
                doc("Walking 30 minutes a day improves cardiovascular health."),
                doc("Agile methodology promotes iterative software development."));
        vectorStore.add(documents);
        log.info("Loaded {} sentence documents into VectorStore (Lesson A, source={})", documents.size(), SOURCE);
    }

    private static Document doc(String text) {
        return new Document(text, Map.of("source", SOURCE));
    }
}
