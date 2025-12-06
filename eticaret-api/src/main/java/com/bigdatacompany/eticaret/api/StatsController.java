package com.bigdatacompany.eticaret.api;

import com.bigdatacompany.eticaret.model.RegionStat;
import com.bigdatacompany.eticaret.model.SearchStat;
import com.bigdatacompany.eticaret.repository.RegionStatRepository;
import com.bigdatacompany.eticaret.repository.SearchStatRepository;
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
            // Son batch'teki arama istatistiklerini getir
            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.sort(Sort.Direction.DESC, "batchId"),
                    Aggregation.group("search").first("count").as("count").first("batchId").as("batchId"),
                    Aggregation.sort(Sort.Direction.DESC, "count"),
                    Aggregation.limit(limit));

            AggregationResults<Map> results = mongoTemplate.aggregate(
                    aggregation, "search_stats", Map.class);

            List<Map<String, Object>> searches = results.getMappedResults().stream()
                    .map(r -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("search", r.get("_id"));
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
            // Son batch'teki bölge istatistiklerini getir
            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.sort(Sort.Direction.DESC, "batchId"),
                    Aggregation.group("region").first("count").as("count").first("batchId").as("batchId"),
                    Aggregation.sort(Sort.Direction.DESC, "count"),
                    Aggregation.limit(limit));

            AggregationResults<Map> results = mongoTemplate.aggregate(
                    aggregation, "region_stats", Map.class);

            List<Map<String, Object>> regions = results.getMappedResults().stream()
                    .map(r -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("region", r.get("_id"));
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
            long totalSearchRecords = searchStatRepository.count();
            long totalRegionRecords = regionStatRepository.count();

            response.put("status", "success");
            response.put("totalSearchRecords", totalSearchRecords);
            response.put("totalRegionRecords", totalRegionRecords);
            response.put("collectionsActive", totalSearchRecords > 0 || totalRegionRecords > 0);

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
            // Top aramaları getir
            Aggregation searchAgg = Aggregation.newAggregation(
                    Aggregation.sort(Sort.Direction.DESC, "batchId"),
                    Aggregation.group("search").first("count").as("count"),
                    Aggregation.sort(Sort.Direction.DESC, "count"),
                    Aggregation.limit(10));

            List<Map<String, Object>> topSearches = mongoTemplate.aggregate(
                    searchAgg, "search_stats", Map.class).getMappedResults().stream()
                    .map(r -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("name", r.get("_id"));
                        item.put("value", r.get("count"));
                        return item;
                    })
                    .collect(Collectors.toList());

            // Bölge dağılımını getir
            Aggregation regionAgg = Aggregation.newAggregation(
                    Aggregation.sort(Sort.Direction.DESC, "batchId"),
                    Aggregation.group("region").first("count").as("count"),
                    Aggregation.sort(Sort.Direction.DESC, "count"),
                    Aggregation.limit(10));

            List<Map<String, Object>> regionDistribution = mongoTemplate.aggregate(
                    regionAgg, "region_stats", Map.class).getMappedResults().stream()
                    .map(r -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("name", r.get("_id"));
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
}
