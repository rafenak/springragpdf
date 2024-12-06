package com.here.faq.backend.controller;

import com.here.faq.backend.service.EmbeddingService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final EmbeddingService embeddingService;

    public ChatController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @PostMapping("/upload")
    public void uploadPdf(@RequestParam("file") MultipartFile file) {
        embeddingService.processAndStorePdf(file);
    }

    @GetMapping("/ask")
    public String askQuestion(@RequestParam("question") String question) {
        return embeddingService.askQuestion(question);
    }
}