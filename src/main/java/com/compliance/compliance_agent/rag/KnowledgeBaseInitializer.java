package com.compliance.compliance_agent.rag;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * 知识库初始化器
 */
@Component
public class KnowledgeBaseInitializer {

    private final DocumentLoader documentLoader;
    private final VectorStore vectorStore;

    public KnowledgeBaseInitializer(DocumentLoader documentLoader, VectorStore vectorStore) {
        this.documentLoader = documentLoader;
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void init() throws IOException {
        List<Document> documents = documentLoader.loadAndSplit("knowledge/java_security_rules.txt");
        vectorStore.add(documents);
        System.out.println("✅ 合规知识库初始化完成，已加载 " + documents.size() + " 个文档块");
    }
}