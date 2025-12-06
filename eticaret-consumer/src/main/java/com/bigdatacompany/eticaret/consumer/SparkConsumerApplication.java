package com.bigdatacompany.eticaret.consumer;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.bson.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.apache.spark.sql.functions.*;

public class SparkConsumerApplication {

    private static final String KAFKA_BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String KAFKA_TOPIC = "search-analysisv2";

    // MongoDB ayarları
    private static final String MONGODB_URI = "mongodb://admin:admin123@localhost:27017/?authSource=admin";
    private static final String MONGODB_DATABASE = "eticaret_analytics";

    // MongoDB client (singleton)
    private static MongoClient mongoClient;
    private static MongoDatabase mongoDatabase;

    public static void main(String[] args) {
        // Windows için Hadoop home directory ayarı
        String hadoopHome = System.getenv("HADOOP_HOME");
        if (hadoopHome == null || hadoopHome.isEmpty()) {
            hadoopHome = "C:\\Users\\yusuf\\hadoop-3.4.1";
        }
        System.setProperty("hadoop.home.dir", hadoopHome);

        // MongoDB bağlantısını kur
        try {
            mongoClient = MongoClients.create(MONGODB_URI);
            mongoDatabase = mongoClient.getDatabase(MONGODB_DATABASE);
            System.out.println("✅ MongoDB bağlantısı kuruldu.");
        } catch (Exception e) {
            System.err.println("❌ MongoDB bağlantı hatası: " + e.getMessage());
            return;
        }

        // JSON schema tanımı
        StructType schema = new StructType()
                .add("search", DataTypes.StringType)
                .add("region", DataTypes.StringType)
                .add("current_ts", DataTypes.StringType)
                .add("timestamp", DataTypes.StringType);

        // Spark Session oluştur
        SparkSession sparkSession = SparkSession.builder()
                .master("local[*]")
                .appName("E-Ticaret Arama Analizi")
                .config("spark.sql.streaming.schemaInference", "true")
                .config("spark.driver.host", "localhost")
                .config("spark.ui.enabled", "false")
                .getOrCreate();

        // Log seviyesini ayarla
        sparkSession.sparkContext().setLogLevel("WARN");

        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║     E-Ticaret Arama Analizi - Spark Streaming + MongoDB      ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  Kafka: " + String.format("%-52s", KAFKA_BOOTSTRAP_SERVERS) + "║");
        System.out.println("║  Topic: " + String.format("%-52s", KAFKA_TOPIC) + "║");
        System.out.println("║  MongoDB: " + String.format("%-50s", "localhost:27017/" + MONGODB_DATABASE) + "║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        try {
            // Kafka'dan veri oku
            Dataset<Row> kafkaDF = sparkSession
                    .readStream()
                    .format("kafka")
                    .option("kafka.bootstrap.servers", KAFKA_BOOTSTRAP_SERVERS)
                    .option("subscribe", KAFKA_TOPIC)
                    .option("startingOffsets", "latest")
                    .option("failOnDataLoss", "false")
                    .load();

            // JSON parse et
            Dataset<Row> parsedDF = kafkaDF
                    .selectExpr("CAST(value AS STRING) as json_value")
                    .select(functions.from_json(col("json_value"), schema).as("data"))
                    .select("data.search", "data.region", "data.current_ts", "data.timestamp");

            // Arama istatistikleri
            Dataset<Row> searchCounts = parsedDF
                    .filter(col("search").isNotNull())
                    .groupBy("search")
                    .count();

            // Bölge istatistikleri
            Dataset<Row> regionCounts = parsedDF
                    .filter(col("search").isNotNull())
                    .groupBy("region")
                    .count();

            // Arama istatistiklerini işle
            searchCounts
                    .writeStream()
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

                            // MongoDB'ye yaz
                            writeToMongoDB(batchDF, "search_stats", "search", batchId);
                        }
                    })
                    .trigger(org.apache.spark.sql.streaming.Trigger.ProcessingTime("5 seconds"))
                    .start();

            // Bölge istatistiklerini işle
            regionCounts
                    .writeStream()
                    .outputMode("complete")
                    .foreachBatch((batchDF, batchId) -> {
                        if (batchDF.count() > 0) {
                            System.out.println("\n🗺️ Bölgelere Göre Arama Dağılımı:");
                            batchDF.orderBy(functions.desc("count")).show(10, false);

                            // MongoDB'ye yaz
                            writeToMongoDB(batchDF, "region_stats", "region", batchId);
                        }
                    })
                    .trigger(org.apache.spark.sql.streaming.Trigger.ProcessingTime("5 seconds"))
                    .start();

            // Her iki stream'in bitmesini bekle
            sparkSession.streams().awaitAnyTermination();

        } catch (Exception e) {
            System.err.println("Streaming hatası: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (mongoClient != null) {
                mongoClient.close();
            }
            sparkSession.stop();
        }
    }

    /**
     * DataFrame verilerini MongoDB'ye yazar
     */
    private static void writeToMongoDB(Dataset<Row> df, String collectionName, String keyField, long batchId) {
        try {
            MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

            List<Row> rows = df.collectAsList();
            List<Document> documents = new ArrayList<>();

            for (Row row : rows) {
                Document doc = new Document();
                doc.append(keyField, row.getString(0));
                doc.append("count", row.getLong(1));
                doc.append("batch_id", batchId);
                doc.append("updated_at", Instant.now().toString());
                documents.add(doc);
            }

            if (!documents.isEmpty()) {
                // Mevcut batch verilerini sil ve yenilerini ekle
                collection.deleteMany(new Document("batch_id", batchId));
                collection.insertMany(documents);
                System.out.println("✅ " + collectionName + " MongoDB'ye kaydedildi. (" + documents.size() + " kayıt)");
            }
        } catch (Exception e) {
            System.err.println("⚠️ MongoDB yazma hatası (" + collectionName + "): " + e.getMessage());
            e.printStackTrace();
        }
    }
}
