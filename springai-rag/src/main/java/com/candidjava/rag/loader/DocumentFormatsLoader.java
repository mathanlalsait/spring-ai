package com.candidjava.rag.loader;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Lesson C: load binary office docs — PDF via PagePdfDocumentReader, DOCX via TikaDocumentReader.
 */
@Component
public class DocumentFormatsLoader {

    public static final String SOURCE = "doc-formats";

    private static final Logger log = LoggerFactory.getLogger(DocumentFormatsLoader.class);

    private final VectorStore vectorStore;

    @Value("classpath:product-faq.pdf")
    private Resource pdfFile;

    @Value("classpath:employee-handbook.docx")
    private Resource docxFile;

    public DocumentFormatsLoader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void load() {
        TextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(120)
                .withMaxNumChunks(50)
                .build();

        List<Document> all = new ArrayList<>();
        all.addAll(readPdf(splitter));
        all.addAll(readDocx(splitter));
        vectorStore.add(all);
        log.info("Loaded {} chunks from PDF+DOCX into VectorStore (Lesson C, source={})", all.size(), SOURCE);
    }

    private List<Document> readPdf(TextSplitter splitter) {
        List<Document> pages = new PagePdfDocumentReader(pdfFile).get();
        List<Document> chunks = tag(splitter.split(pages), "pdf", pdfFile.getFilename());
        log.info("PDF '{}' → {} page docs → {} chunks", pdfFile.getFilename(), pages.size(), chunks.size());
        return chunks;
    }

    private List<Document> readDocx(TextSplitter splitter) {
        List<Document> docs = new TikaDocumentReader(docxFile).get();
        List<Document> chunks = tag(splitter.split(docs), "docx", docxFile.getFilename());
        log.info("DOCX '{}' → {} docs → {} chunks", docxFile.getFilename(), docs.size(), chunks.size());
        return chunks;
    }

    private static List<Document> tag(List<Document> docs, String format, String filename) {
        return docs.stream()
                .map(doc -> doc.mutate()
                        .metadata("source", SOURCE)
                        .metadata("format", format)
                        .metadata("filename", filename != null ? filename : format)
                        .build())
                .toList();
    }
}
