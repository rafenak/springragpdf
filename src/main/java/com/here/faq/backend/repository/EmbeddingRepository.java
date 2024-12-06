package com.here.faq.backend.repository;

import com.here.faq.backend.modal.Embedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmbeddingRepository extends JpaRepository<Embedding, Long> {

    @Query(value = "SELECT text FROM document_embeddings ORDER BY embedding <-> cast(:queryEmbedding AS vector) LIMIT 5", nativeQuery = true)
    List<String> findRelevantEmbeddings(@Param("queryEmbedding") String queryEmbedding);


}
