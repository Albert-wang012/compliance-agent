package com.compliance.compliance_agent.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * 文档加载器
 */
@Component
public class DocumentLoader {

    private final ResourceLoader resourceLoader;

    public DocumentLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public List<Document> loadAndSplit(String path) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + path);
        TextReader reader = new TextReader(resource);
        List<Document> documents = reader.get();

        TokenTextSplitter splitter = new TokenTextSplitter();
        return splitter.apply(documents);
    }
}