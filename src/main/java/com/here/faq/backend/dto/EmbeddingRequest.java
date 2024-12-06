package com.here.faq.backend.dto;

public class EmbeddingRequest {
    private String prompt;
    private String model;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }

    public EmbeddingRequest(String prompt) {
        this.prompt = prompt;
    }

    public EmbeddingRequest(String prompt,String model) {
        this.prompt = prompt;
        this.model = model;
    }

    public  EmbeddingRequest(){
    }

}
