package com.bigdatacompany.eticaret.api;

import com.bigdatacompany.eticaret.MessageProducer;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api")
public class SearchController {

    @Autowired
    private MessageProducer messageProducer;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicLong messageCount = new AtomicLong(0);

    // Simülasyon verileri
    private static final List<String> CITIES = Arrays.asList(
            "Ankara", "İstanbul", "Mersin", "Gaziantep", "Samsun",
            "Ordu", "İzmir", "Bursa", "Antalya", "Adana");

    private static final List<String> PRODUCTS = Arrays.asList(
            "bebek bezi", "telefon", "televizyon", "ayakkabı", "havlu",
            "kitap", "laptop", "kulaklık", "saat", "çanta");

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> startSearch(
            @RequestParam(required = false, defaultValue = "1000") int intervalMs) {

        Map<String, Object> response = new HashMap<>();

        if (isRunning.get()) {
            response.put("status", "error");
            response.put("message", "Veri üretimi zaten çalışıyor!");
            response.put("messageCount", messageCount.get());
            return ResponseEntity.badRequest().body(response);
        }

        isRunning.set(true);
        messageCount.set(0);

        CompletableFuture.runAsync(() -> {
            Random random = new Random();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            try {
                while (isRunning.get()) {
                    int cityIndex = random.nextInt(CITIES.size());
                    int productIndex = random.nextInt(PRODUCTS.size());

                    // Rastgele tarih üret (son 4 yıl içinde)
                    long offset = Timestamp.valueOf("2020-09-09 00:00:00").getTime();
                    long end = Timestamp.valueOf("2024-09-09 00:00:00").getTime();
                    long diff = end - offset + 1;
                    Timestamp rand = new Timestamp(offset + (long) (Math.random() * diff));

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("search", PRODUCTS.get(productIndex));
                    jsonObject.put("current_ts", rand.toString());
                    jsonObject.put("region", CITIES.get(cityIndex));
                    jsonObject.put("timestamp", LocalDateTime.now().format(formatter));

                    String message = jsonObject.toJSONString();
                    System.out.println("[" + messageCount.incrementAndGet() + "] " + message);
                    messageProducer.send(message);

                    Thread.sleep(intervalMs);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Veri üretimi durduruldu.");
            } catch (Exception e) {
                System.err.println("Hata oluştu: " + e.getMessage());
                e.printStackTrace();
            } finally {
                isRunning.set(false);
            }
        });

        response.put("status", "success");
        response.put("message", "Veri üretimi başlatıldı!");
        response.put("interval", intervalMs + "ms");
        response.put("stopEndpoint", "/api/stop");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopProducing() {
        Map<String, Object> response = new HashMap<>();

        if (!isRunning.get()) {
            response.put("status", "error");
            response.put("message", "Veri üretimi zaten durmuş!");
            return ResponseEntity.badRequest().body(response);
        }

        isRunning.set(false);
        response.put("status", "success");
        response.put("message", "Veri üretimi durduruluyor...");
        response.put("totalMessages", messageCount.get());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("isRunning", isRunning.get());
        response.put("messageCount", messageCount.get());
        response.put("cities", CITIES);
        response.put("products", PRODUCTS);
        return ResponseEntity.ok(response);
    }
}
