package com.bigdatacompany.eticaret.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Arama istatistikleri için MongoDB entity
 */
@Document(collection = "search_stats")
public class SearchStat {

    @Id
    private String id;

    private String search;
    private Long count;
    private Long batchId;
    private LocalDateTime updatedAt;

    // Constructors
    public SearchStat() {
    }

    public SearchStat(String search, Long count) {
        this.search = search;
        this.count = count;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
