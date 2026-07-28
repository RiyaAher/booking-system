package com.turf.booking_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TurfAiService {

    private static final Logger log = LoggerFactory.getLogger(TurfAiService.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public TurfAiService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    public String askAi(String userMessage) {
        try {
            log.info("--> [USER QUERY]: {}", userMessage);

            // 1. Fetch relevant policy snippets from VectorStore
            List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.query(userMessage).withTopK(3)
            );

            String policyContext = similarDocuments.stream()
                    .map(doc -> doc.getContent())
                    .collect(Collectors.joining("\n---\n"));

            log.info("--> [RAG CONTEXT RETRIEVED]:\n{}", 
                policyContext.isEmpty() ? "NO MATCHING CONTEXT FOUND" : policyContext);

            String currentContext = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("EEEE, yyyy-MM-dd HH:mm")
            );

            // 2. Pass context + function tools to Ollama
            String response = this.chatClient.prompt()
                .system("""
                    You are the official AI Concierge for Turf Arena.
                    Current reference date/time: %s.
                    
                    RELEVANT TURF POLICIES & RULES:
                    %s
                    
                    STRICT RULES:
                    1. Answer questions about footwear, rules, amenities, and policies ONLY using the RELEVANT TURF POLICIES & RULES section above.
                    2. If the answer is directly stated in the policies above, do NOT invoke any availability or pricing tools.
                    3. Only invoke 'checkAvailabilityTool' or 'calculatePriceTool' if the user explicitly asks to check booking availability, time slots, or calculate price.
                    """.formatted(currentContext, policyContext))
                .user(userMessage)
                .functions("checkAvailabilityTool", "calculatePriceTool")
                .call()
                .content();

            log.info("--> [AI RESPONSE]: {}", response);
            return response;

        } catch (Exception e) {
            log.error("--> [AI ERROR]: ", e);
            return "Sorry, I ran into an issue getting that information ☹️ Please try again!";
        }
    }
}