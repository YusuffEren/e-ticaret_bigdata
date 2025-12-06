package com.bigdatacompany.eticaret.consumer;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

import static org.apache.spark.sql.functions.*;

public class SparkConsumerApplication {

    private static final String KAFKA_BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String KAFKA_TOPIC = "search-analysisv2";

    public static void main(String[] args) {
        // Windows için Hadoop home directory ayarı
        // Bu path'i kendi sisteminize göre değiştirin veya environment variable olarak
        // ayarlayın
        String hadoopHome = System.getenv("HADOOP_HOME");
        if (hadoopHome == null || hadoopHome.isEmpty()) {
            hadoopHome = "C:\\Users\\yusuf\\hadoop-3.4.1";
        }
        System.setProperty("hadoop.home.dir", hadoopHome);

        // JSON schema tanımı
        StructType schema = new StructType()
                .add("search", DataTypes.StringType)
                .add("region", DataTypes.StringType)
                .add("current_ts", DataTypes.StringType);

        // Spark Session oluştur
        SparkSession sparkSession = SparkSession.builder()
                .master("local[*]") // Tüm CPU core'larını kullan
                .appName("E-Ticaret Arama Analizi")
                .config("spark.sql.streaming.schemaInference", "true")
                .config("spark.driver.host", "localhost")
                .config("spark.ui.enabled", "false") // Web UI'ı devre dışı bırak (opsiyonel)
                .getOrCreate();

        // Log seviyesini ayarla (daha az log için)
        sparkSession.sparkContext().setLogLevel("WARN");

        System.out.println("========================================");
        System.out.println("E-Ticaret Arama Analizi Başlatılıyor...");
        System.out.println("Kafka: " + KAFKA_BOOTSTRAP_SERVERS);
        System.out.println("Topic: " + KAFKA_TOPIC);
        System.out.println("========================================");

        // Kafka'dan stream okuma
        Dataset<Row> kafkaStream = sparkSession.readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", KAFKA_BOOTSTRAP_SERVERS)
                .option("subscribe", KAFKA_TOPIC)
                .option("startingOffsets", "earliest")
                .option("failOnDataLoss", "false")
                .load();

        // Kafka value'sunu String olarak parse et
        Dataset<Row> valueDataset = kafkaStream.selectExpr("CAST(value AS STRING) as json_value");

        // JSON'ı struct'a çevir
        Dataset<Row> parsedDataset = valueDataset
                .select(from_json(col("json_value"), schema).as("data"))
                .select("data.*")
                .filter(col("search").isNotNull()); // Null kayıtları filtrele

        // Arama terimlerine göre gruplama
        Dataset<Row> searchCounts = parsedDataset.groupBy("search").count();

        // Bölgelere göre gruplama
        Dataset<Row> regionCounts = parsedDataset.groupBy("region").count();

        try {
            // Arama istatistiklerini stream olarak yaz
            searchCounts.writeStream()
                    .outputMode("complete")
                    .foreachBatch((batchDF, batchId) -> {
                        if (batchDF.count() > 0) {
                            System.out.println("\n╔══════════════════════════════════════════╗");
                            System.out.println(
                                    "║  ARAMA İSTATİSTİKLERİ - Batch: " + String.format("%-9d", batchId) + "║");
                            System.out.println("╠══════════════════════════════════════════╣");
                            System.out.println(
                                    "║  Toplam farklı arama: " + String.format("%-18d", batchDF.count()) + "║");
                            System.out.println("╚══════════════════════════════════════════╝");

                            System.out.println("\n📊 En Çok Aranan Ürünler:");
                            batchDF.orderBy(functions.desc("count")).show(10, false);
                        }
                    })
                    .trigger(org.apache.spark.sql.streaming.Trigger.ProcessingTime("5 seconds"))
                    .start()
                    .awaitTermination();

        } catch (Exception e) {
            System.err.println("Streaming hatası: " + e.getMessage());
            e.printStackTrace();
        } finally {
            sparkSession.stop();
        }
    }
}
