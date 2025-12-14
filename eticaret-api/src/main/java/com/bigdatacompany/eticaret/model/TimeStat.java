package com.bigdatacompany.eticaret.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * Zaman bazlı istatistikler için MongoDB entity
 * Saatlik ve günlük arama trendlerini tutar
 */
@Document(collection = "time_stats")
public class TimeStat {

    @Id
    private String id;

    @Field("hour")
    private Integer hour;  // 0-23 arası saat

    @Field("date")
    private String date;   // YYYY-MM-DD formatında tarih

    @Field("count")
    private Long count;    // O saat/gündeki arama sayısı

    @Field("batch_id")
    private Long batchId;

    @Field("updated_at")
    private String updatedAt;

    // Constructors
    public TimeStat() {
    }

    public TimeStat(Integer hour, String date, Long count) {
        this.hour = hour;
        this.date = date;
        this.count = count;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}

