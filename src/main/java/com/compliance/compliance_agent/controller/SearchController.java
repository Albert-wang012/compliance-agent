package com.compliance.compliance_agent.controller;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 检索测试接口
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final VectorStore vectorStore;

    public SearchController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @GetMapping("/test")
    public List<Map<String, Object>> test(@RequestParam String query) {
        return vectorStore.similaritySearch(query)
                .stream()
                .map(doc -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("content", doc.getText());
                    map.put("score", doc.getScore());
                    return map;
                })
                .toList();
    }
}