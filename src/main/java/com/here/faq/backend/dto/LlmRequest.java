package com.here.faq.backend.dto;

public class LlmRequest {

    private String prompt;
    private String model;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public LlmRequest(){
    }

    public LlmRequest(String prompt, String model) {
        this.prompt = prompt;
        this.model = model;
    }
}
