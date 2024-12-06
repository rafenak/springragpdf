package com.here.faq.backend.dto;


public class EmbeddingResponse {

    private float[] embedding;

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public EmbeddingResponse(float[] embedding) {
        this.embedding = embedding;
    }

    public EmbeddingResponse(){

    }
}
