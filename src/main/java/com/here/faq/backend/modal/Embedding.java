package com.here.faq.backend.modal;

import jakarta.persistence.*;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "document_embeddings")
public class Embedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String documentId;

    @Column(columnDefinition="text", length=10485760)
    private String text;

//    @Column( name = "embedding" )
//    @JdbcTypeCode(SqlTypes.VECTOR)
//    @Array(length = 3)
//    private float[] embedding;

    @Column(name = "embedding", columnDefinition = "vector(4096)")
    private float[] embedding;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }




}