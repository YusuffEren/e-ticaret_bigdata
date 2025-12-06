package com.bigdatacompany.eticaret.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Bölge istatistikleri için MongoDB entity
 */
@Document(collection = "region_stats")
public class RegionStat {

    @Id
    private String id;

    private String region;
    private Long count;
    private Long batchId;
    private LocalDateTime updatedAt;

    // Constructors
    public RegionStat() {
    }

    public RegionStat(String region, Long count) {
        this.region = region;
        this.count = count;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
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
