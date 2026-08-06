package com.turf.booking_system.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class VectorStoreConfig {

    @Value("classpath:turf-rules.txt")
    private Resource rulesFile;

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore vectorStore = new SimpleVectorStore(embeddingModel);
        File vectorFile = new File("vector-store.json");

        if (vectorFile.exists()) {
            // Load existing local ONNX embeddings
            vectorStore.load(vectorFile);
        } else {
            // Auto-close input streams when reading completes
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(rulesFile.getInputStream(), StandardCharsets.UTF_8))) {
                
                String content = reader.lines().collect(Collectors.joining("\n"));

                Document doc = new Document(content);
                vectorStore.add(List.of(doc));
                
                // Save new ONNX embeddings to local JSON
                vectorStore.save(vectorFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return vectorStore;
    }
}