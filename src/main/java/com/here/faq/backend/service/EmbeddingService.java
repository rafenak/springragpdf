package com.here.faq.backend.service;

import com.google.gson.*;
import com.here.faq.backend.dto.EmbeddingRequest;
import com.here.faq.backend.dto.EmbeddingResponse;
import com.here.faq.backend.dto.LlmRequest;
import com.here.faq.backend.modal.Embedding;
import com.here.faq.backend.repository.EmbeddingRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class EmbeddingService {

    private final WebClient webClient;
    private final EmbeddingRepository embeddingRepository;

    @Value("${ollama.api.embedding.url}")
    private String ollamaApiEmbeddingUrl;

    @Value("${spring.ai.ollama.chat.model}")
    private String model;

    @Value("${ollama.api.generate.url}")
    private String ollamaApiGenerateUrl;

    public EmbeddingService(WebClient.Builder webClientBuilder, EmbeddingRepository embeddingRepository) {
        this.webClient = webClientBuilder.build();
        this.embeddingRepository = embeddingRepository;
    }

//    public void processAndStorePdf(MultipartFile file) {
//        try (InputStream inputStream = file.getInputStream()) {
//            // Use Apache Tika to extract text from the PDF
//            String text = extractTextWithPdfBox(inputStream);
//            System.out.println("text length" +text.length());
//
//            // Split the extracted text into manageable chunks and store embeddings
//            for (String chunk : splitTextIntoChunks(text)) {
//                if (chunk.trim().isEmpty()) {
//                    continue; // Skip empty chunks
//                }
//                System.out.println("Processing chunk (size: " + chunk.length() + "): ");
//                float[] embedding = getEmbedding(chunk);
//                saveEmbedding(file.getOriginalFilename(), chunk, embedding);
//            }
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to process PDF with Apache Tika", e);
//        }
//    }

    // Method to process PDF and store embeddings
    public void processAndStorePdf(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            String text = extractTextWithPdfBox(inputStream);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (String chunk : splitTextIntoChunks(text)) {
                if (chunk.trim().isEmpty()) {
                    continue;
                }
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        System.out.println("Processing chunk (size: " + chunk.length() + "): ");
                        float[] embedding = getEmbedding(chunk);
                        saveEmbedding(file.getOriginalFilename(), chunk, embedding);
                    } catch (Exception e) {
                        System.err.println("Error processing chunk: " + e.getMessage());
                        e.printStackTrace();
                    }
                }));
            }

            // Wait for all futures to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (IOException e) {
            throw new RuntimeException("Failed to process PDF file: " + file.getOriginalFilename(), e);
        }
    }


    public String askQuestion(String question) {
        float[] questionEmbedding = getEmbedding(question);
        String embeddingString = convertEmbeddingToString(questionEmbedding);
        List<String> context;
        try {
            context = embeddingRepository.findRelevantEmbeddings(embeddingString);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve relevant embeddings", e);
        }

        if (context.isEmpty()) {
            return "No relevant context found for the question.";
        }
        String combinedContext = String.join("\n", context);
        String prompt = "Context:\n" + combinedContext + "\n\nQuestion:\n" + question;

        StringBuilder completeResponse = new StringBuilder();
        try {
            String llmResponse = webClient.post()
                    .uri(ollamaApiGenerateUrl)
                    .bodyValue(new LlmRequest(prompt,model))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            String[] responseParts = llmResponse.split("\n");

            for (String responsePart : responseParts) {
                try {
                    JsonObject responseObject = JsonParser.parseString(responsePart).getAsJsonObject();
                    String responseText = responseObject.get("response").getAsString();
                    completeResponse.append(responseText);

                    if (responseObject.has("done") && responseObject.get("done").getAsBoolean()) {
                        break;
                    }
                } catch (Exception e) {
                    continue;
                }
            }

            return completeResponse.toString();
           // return llmResponse;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate response from LLM", e);
        }
    }


    private float[] getEmbedding(String text) {
        try {
            EmbeddingResponse response = webClient.post()
                    .uri(ollamaApiEmbeddingUrl)
                    .bodyValue(new EmbeddingRequest(text, model))
                    .retrieve()
                    .bodyToMono(EmbeddingResponse.class)
                    .block();
            return response.getEmbedding();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve embedding", e);
        }
    }

    private void saveEmbedding(String documentId, String text, float[] embedding) {
        Embedding entity = new Embedding();
        entity.setDocumentId(documentId);
        entity.setText(text);
        entity.setEmbedding(embedding);
        try {
            embeddingRepository.save(entity);
        } catch (Exception e) {
            System.err.println("Error saving embedding for document " + documentId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

//    private List<String> splitTextIntoChunks(String text) {
//        List<String> chunks = new ArrayList<>();
//        BreakIterator iterator = BreakIterator.getSentenceInstance();
//        iterator.setText(text);
//        StringBuilder chunk = new StringBuilder();
//        int start = iterator.first();
//        int chunkLimit = 5000;  // Adjust chunk size limit for larger documents
//        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
//            if (chunk.length() + (end - start) > chunkLimit) {
//                chunks.add(chunk.toString());
//                chunk = new StringBuilder();
//            }
//            chunk.append(text, start, end).append(" ");
//        }
//        if (chunk.length() > 0) {
//            chunks.add(chunk.toString());
//        }
//        return chunks;
//    }

    private List<String> splitTextIntoChunks(String text) {
        List<String> chunks = new ArrayList<>();
        BreakIterator iterator = BreakIterator.getSentenceInstance();
        iterator.setText(text);
        StringBuilder chunk = new StringBuilder();
        int start = iterator.first();
        int chunkLimit = 5000;  // Adjust chunk size limit for larger documents
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            if (chunk.length() + (end - start) > chunkLimit) {
                chunks.add(chunk.toString());
                chunk = new StringBuilder();
            }
            chunk.append(text, start, end).append(" ");
        }
        if (chunk.length() > 0) {
            chunks.add(chunk.toString());
        }
        return chunks;
    }


//    private String extractTextWithTika(InputStream inputStream) throws TikaException, SAXException, IOException {
//        BodyContentHandler handler = new BodyContentHandler(-1); // No character limit
//        Metadata metadata = new Metadata();
//        PDFParser parser = new PDFParser();
//        parser.parse(inputStream, handler, metadata, new ParseContext());
//        return handler.toString();
//    }

    private String extractTextWithPdfBox(InputStream inputStream) throws IOException {
        PDDocument document = Loader.loadPDF(inputStream.readAllBytes());
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        stripper.setStartPage(1);
        stripper.setEndPage(document.getNumberOfPages());

        // Extract the full text
        String text = stripper.getText(document);
        document.close();
        return text;
    }

    private String convertEmbeddingToString(float[] embedding) {
        return Arrays.toString(embedding);
    }

}
