package com.bigdatacompany.eticaret;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.Future;

@Component
public class MessageProducer {

    private Producer<String, String> producer;

    @Value("${kafka.bootstrap.servers}")
    private String bootstrapServers;

    @Value("${kafka.topic:search-analysisv2}")
    private String topic;

    @PostConstruct
    public void init() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Güvenilirlik ayarları
        config.put(ProducerConfig.ACKS_CONFIG, "1"); // Leader'dan onay bekle
        config.put(ProducerConfig.RETRIES_CONFIG, 3); // 3 kez tekrar dene
        config.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000); // Tekrar denemeler arası 1 saniye bekle

        // Performans ayarları
        config.put(ProducerConfig.LINGER_MS_CONFIG, 5); // 5ms bekle batch oluşturmak için
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // 16KB batch size

        try {
            producer = new KafkaProducer<>(config);
            System.out.println("Kafka Producer başarıyla oluşturuldu. Server: " + bootstrapServers);
        } catch (Exception e) {
            System.err.println("Kafka Producer oluşturulurken hata: " + e.getMessage());
            throw e;
        }
    }

    public void send(String message) {
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);
            Future<RecordMetadata> future = producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    System.err.println("Mesaj gönderilemedi: " + exception.getMessage());
                }
            });
            // Non-blocking - callback ile hata yakalama
        } catch (Exception e) {
            System.err.println("Kafka'ya mesaj gönderilirken hata: " + e.getMessage());
        }
    }

    public void sendSync(String message) throws Exception {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);
        RecordMetadata metadata = producer.send(record).get();
        System.out.println("Mesaj gönderildi - Topic: " + metadata.topic() +
                ", Partition: " + metadata.partition() +
                ", Offset: " + metadata.offset());
    }

    @PreDestroy
    public void close() {
        if (producer != null) {
            producer.flush();
            producer.close();
            System.out.println("Kafka Producer kapatıldı.");
        }
    }
}
