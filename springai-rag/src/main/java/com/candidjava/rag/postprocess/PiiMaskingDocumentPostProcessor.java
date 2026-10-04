package com.candidjava.rag.postprocess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Advanced RAG lesson: mask PII in retrieved documents before they enter the prompt.
 */
public class PiiMaskingDocumentPostProcessor implements DocumentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(PiiMaskingDocumentPostProcessor.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "\\b(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}\\b");

    private PiiMaskingDocumentPostProcessor() {
    }

    public static PiiMaskingDocumentPostProcessor create() {
        return new PiiMaskingDocumentPostProcessor();
    }

    @Override
    public List<Document> process(Query query, List<Document> documents) {
        Assert.notNull(query, "query cannot be null");
        Assert.notNull(documents, "documents cannot be null");
        if (CollectionUtils.isEmpty(documents)) {
            return documents;
        }
        log.info("PII masking post-processor running for query='{}', docs={}", query.text(), documents.size());
        return documents.stream()
                .map(document -> {
                    String text = document.getText() != null ? document.getText() : "";
                    String masked = EMAIL_PATTERN.matcher(text).replaceAll("[REDACTED_EMAIL]");
                    masked = PHONE_PATTERN.matcher(masked).replaceAll("[REDACTED_PHONE]");
                    return document.mutate().text(masked).metadata("pii_masked", true).build();
                })
                .toList();
    }
}
