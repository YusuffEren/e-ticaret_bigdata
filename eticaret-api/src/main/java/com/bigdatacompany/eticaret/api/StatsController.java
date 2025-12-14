package com.bigdatacompany.eticaret.api;

import com.bigdatacompany.eticaret.model.RegionStat;
import com.bigdatacompany.eticaret.model.SearchStat;
import com.bigdatacompany.eticaret.model.TimeStat;
import com.bigdatacompany.eticaret.repository.RegionStatRepository;
import com.bigdatacompany.eticaret.repository.SearchStatRepository;
import com.bigdatacompany.eticaret.repository.TimeStatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * İstatistik endpoint'leri - MongoDB'den veri okuma
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private SearchStatRepository searchStatRepository;

    @Autowired
    private RegionStatRepository regionStatRepository;

    @Autowired
    private TimeStatRepository timeStatRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * En çok aranan ürünleri getir
     * GET /api/stats/searches?limit=10
     */
    @GetMapping("/searches")
    public ResponseEntity<Map<String, Object>> getTopSearches(
            @RequestParam(required = false, defaultValue = "10") int limit) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Arama istatistiklerini doğrudan getir (veriler zaten güncel)
            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.sort(Sort.Direction.DESC, "count"),
                    Aggregation.limit(limit));

            AggregationResults<Map> results = mongoTemplate.aggregate(
                    aggregation, "search_stats", Map.class);

            List<Map<String, Object>> searches = results.getMappedResults().stream()
                    .map(r -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("search", r.get("search"));
                        item.put("count", r.get("count"));
                        return item;
                    })
                    .collect(Collectors.toList());

            response.put("status", "success");
            response.put("data", searches);
            response.put("total", searches.size());

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Veri alınırken hata oluştu: " + e.getMessage());
            response.put("data", List.of());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Bölgelere göre arama dağılımını getir
     * GET /api/stats/regions?limit=10
     */
    @GetMapping("/regions")
    public ResponseEntity<Map<String, Object>> getRegionStats(
            @RequestParam(required = false, defaultValue = "10") int limit) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Bölge istatistiklerini doğrudan getir (veriler zaten güncel)
            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.sort(Sort.Direction.DESC, "count"),
                    Aggregation.limit(limit));

            AggregationResults<Map> results = mongoTemplate.aggregate(
                    aggregation, "region_stats", Map.class);

            List<Map<String, Object>> regions = results.getMappedResults().stream()
                    .map(r -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("region", r.get("region"));
                        item.put("count", r.get("count"));
                        return item;
                    })
                    .collect(Collectors.toList());

            response.put("status", "success");
            response.put("data", regions);
            response.put("total", regions.size());

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Veri alınırken hata oluştu: " + e.getMessage());
            response.put("data", List.of());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Özet istatistikleri getir
     * GET /api/stats/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Toplam arama sayısını hesapla (tüm count'ların toplamı)
            Aggregation searchSumAgg = Aggregation.newAggregation(
                    Aggregation.group().sum("count").as("total"));
            AggregationResults<Map> searchSum = mongoTemplate.aggregate(
                    searchSumAgg, "search_stats", Map.class);
            long totalSearches = 0;
            if (!searchSum.getMappedResults().isEmpty()) {
                Object total = searchSum.getMappedResults().get(0).get("total");
                totalSearches = total != null ? ((Number) total).longValue() : 0;
            }

            // Farklı arama terimi sayısı
            long uniqueSearchTerms = searchStatRepository.count();
            
            // Farklı bölge sayısı
            long uniqueRegions = regionStatRepository.count();

            response.put("status", "success");
            response.put("totalSearchRecords", totalSearches);  // Toplam arama sayısı
            response.put("totalRegionRecords", uniqueRegions);  // Farklı bölge sayısı
            response.put("uniqueSearchTerms", uniqueSearchTerms);  // Farklı arama terimi sayısı
            response.put("collectionsActive", totalSearches > 0);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Özet bilgisi alınırken hata: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Dashboard için tüm verileri tek seferde getir
     * GET /api/stats/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Top aramaları doğrudan getir
            Aggregation searchAgg = Aggregation.newAggregation(
                    Aggregation.sort(Sort.Direction.DESC, "count"),
                    Aggregation.limit(10));

            List<Map<String, Object>> topSearches = mongoTemplate.aggregate(
                    searchAgg, "search_stats", Map.class).getMappedResults().stream()
                    .map(r -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("name", r.get("search"));
                        item.put("value", r.get("count"));
                        return item;
                    })
                    .collect(Collectors.toList());

            // Bölge dağılımını doğrudan getir
            Aggregation regionAgg = Aggregation.newAggregation(
                    Aggregation.sort(Sort.Direction.DESC, "count"),
                    Aggregation.limit(10));

            List<Map<String, Object>> regionDistribution = mongoTemplate.aggregate(
                    regionAgg, "region_stats", Map.class).getMappedResults().stream()
                    .map(r -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("name", r.get("region"));
                        item.put("value", r.get("count"));
                        return item;
                    })
                    .collect(Collectors.toList());

            response.put("status", "success");
            response.put("topSearches", topSearches);
            response.put("regionDistribution", regionDistribution);
            response.put("lastUpdated", System.currentTimeMillis());

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Dashboard verisi alınırken hata: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Saatlik arama istatistiklerini getir (son 24 saat)
     * GET /api/stats/hourly
     */
    @GetMapping("/hourly")
    public ResponseEntity<Map<String, Object>> getHourlyStats() {
        Map<String, Object> response = new HashMap<>();

        try {
            // time_stats koleksiyonundan saatlik verileri getir
            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.group("hour").sum("count").as("totalCount"),
                    Aggregation.sort(Sort.Direction.ASC, "_id"));

            AggregationResults<Map> results = mongoTemplate.aggregate(
                    aggregation, "time_stats", Map.class);

            // 0-23 saat için veri hazırla (eksik saatler için 0)
            Map<Integer, Long> hourlyData = new HashMap<>();
            for (int i = 0; i < 24; i++) {
                hourlyData.put(i, 0L);
            }

            // MongoDB'den gelen verileri ekle
            for (Map r : results.getMappedResults()) {
                Integer hour = (Integer) r.get("_id");
                Long count = ((Number) r.get("totalCount")).longValue();
                if (hour != null) {
                    hourlyData.put(hour, count);
                }
            }

            // Sıralı liste oluştur
            List<Map<String, Object>> hourlyList = new ArrayList<>();
            for (int i = 0; i < 24; i++) {
                Map<String, Object> item = new HashMap<>();
                item.put("hour", String.format("%02d:00", i));
                item.put("count", hourlyData.get(i));
                hourlyList.add(item);
            }

            response.put("status", "success");
            response.put("data", hourlyList);
            response.put("lastUpdated", System.currentTimeMillis());

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Saatlik veri alınırken hata: " + e.getMessage());
            response.put("data", List.of());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Günlük arama istatistiklerini getir (son 7 gün)
     * GET /api/stats/daily
     */
    @GetMapping("/daily")
    public ResponseEntity<Map<String, Object>> getDailyStats() {
        Map<String, Object> response = new HashMap<>();

        try {
            // time_stats koleksiyonundan günlük verileri getir
            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.group("date").sum("count").as("totalCount"),
                    Aggregation.sort(Sort.Direction.ASC, "_id"),
                    Aggregation.limit(7));

            AggregationResults<Map> results = mongoTemplate.aggregate(
                    aggregation, "time_stats", Map.class);

            // Son 7 gün için veri hazırla
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            Map<String, Long> dailyData = new HashMap<>();
            
            for (int i = 6; i >= 0; i--) {
                String dateStr = today.minusDays(i).format(formatter);
                dailyData.put(dateStr, 0L);
            }

            // MongoDB'den gelen verileri ekle
            for (Map r : results.getMappedResults()) {
                String date = (String) r.get("_id");
                Long count = ((Number) r.get("totalCount")).longValue();
                if (date != null && dailyData.containsKey(date)) {
                    dailyData.put(date, count);
                }
            }

            // Sıralı liste oluştur
            List<Map<String, Object>> dailyList = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                String dateStr = today.minusDays(i).format(formatter);
                Map<String, Object> item = new HashMap<>();
                item.put("date", dateStr);
                item.put("count", dailyData.get(dateStr));
                dailyList.add(item);
            }

            response.put("status", "success");
            response.put("data", dailyList);
            response.put("lastUpdated", System.currentTimeMillis());

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Günlük veri alınırken hata: " + e.getMessage());
            response.put("data", List.of());
        }

        return ResponseEntity.ok(response);
    }
}
