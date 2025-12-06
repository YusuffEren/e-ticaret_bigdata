package com.bigdatacompany.eticaret.consumer;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;

import static org.apache.spark.sql.functions.*;

/**
 * Spark Batch Application
 * MongoDB'deki geçmiş verileri analiz eder ve özet raporlar oluşturur
 * 
 * Kullanım: mvnw.cmd exec:java
 * -Dexec.mainClass="com.bigdatacompany.eticaret.consumer.SparkBatchApplication"
 */
public class SparkBatchApplication {

    // MongoDB ayarları
    private static final String MONGODB_URI = "mongodb://admin:admin123@localhost:27017";
    private static final String MONGODB_DATABASE = "eticaret_analytics";

    public static void main(String[] args) {
        // Windows için Hadoop home directory ayarı
        String hadoopHome = System.getenv("HADOOP_HOME");
        if (hadoopHome == null || hadoopHome.isEmpty()) {
            hadoopHome = "C:\\Users\\yusuf\\hadoop-3.4.1";
        }
        System.setProperty("hadoop.home.dir", hadoopHome);

        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           E-Ticaret Batch Analizi - Spark Batch              ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  MongoDB: " + String.format("%-50s", "localhost:27017/" + MONGODB_DATABASE) + "║");
        System.out.println("║  Mod: Batch Processing                                       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        // Spark Session oluştur
        SparkSession spark = SparkSession.builder()
                .master("local[*]")
                .appName("E-Ticaret Batch Analizi")
                .config("spark.driver.host", "localhost")
                .config("spark.ui.enabled", "false")
                .getOrCreate();

        // Log seviyesini ayarla
        spark.sparkContext().setLogLevel("WARN");

        try {
            // 1. MongoDB'den arama istatistiklerini oku
            System.out.println("\n📊 Arama istatistikleri okunuyor...");
            Dataset<Row> searchStats = spark.read()
                    .format("mongodb")
                    .option("connection.uri", MONGODB_URI)
                    .option("database", MONGODB_DATABASE)
                    .option("collection", "search_stats")
                    .load();

            long searchCount = searchStats.count();
            System.out.println("   Toplam arama kaydı: " + searchCount);

            // 2. MongoDB'den bölge istatistiklerini oku
            System.out.println("\n🗺️ Bölge istatistikleri okunuyor...");
            Dataset<Row> regionStats = spark.read()
                    .format("mongodb")
                    .option("connection.uri", MONGODB_URI)
                    .option("database", MONGODB_DATABASE)
                    .option("collection", "region_stats")
                    .load();

            long regionCount = regionStats.count();
            System.out.println("   Toplam bölge kaydı: " + regionCount);

            if (searchCount == 0 && regionCount == 0) {
                System.out.println("\n⚠️ MongoDB'de veri bulunamadı. Önce Spark Streaming uygulamasını çalıştırın.");
                spark.stop();
                return;
            }

            // 3. Arama Özet Raporu
            System.out.println("\n" + "=".repeat(60));
            System.out.println("📈 ARAMA ÖZET RAPORU");
            System.out.println("=".repeat(60));

            if (searchCount > 0) {
                // Tüm zamanların en çok aranan ürünleri
                Dataset<Row> topSearchesAllTime = searchStats
                        .groupBy("search")
                        .agg(functions.sum("count").as("total_count"))
                        .orderBy(functions.desc("total_count"))
                        .limit(10);

                System.out.println("\n🏆 Tüm Zamanların En Çok Aranan 10 Ürünü:");
                topSearchesAllTime.show(false);

                // Batch başına ortalama arama sayısı
                Dataset<Row> avgSearchPerBatch = searchStats
                        .groupBy("batch_id")
                        .agg(functions.avg("count").as("avg_count"))
                        .agg(functions.avg("avg_count").as("overall_avg"));

                System.out.println("\n📊 Batch Başına Ortalama Arama:");
                avgSearchPerBatch.show(false);

                // Toplam farklı arama terimi sayısı
                long uniqueSearches = searchStats.select("search").distinct().count();
                System.out.println("🔍 Toplam Farklı Arama Terimi: " + uniqueSearches);

                // Sonuçları batch_reports collection'ına kaydet
                Dataset<Row> searchReport = topSearchesAllTime
                        .withColumn("report_type", lit("all_time_top_searches"))
                        .withColumn("generated_at", current_timestamp());

                searchReport.write()
                        .format("mongodb")
                        .mode(SaveMode.Append)
                        .option("connection.uri", MONGODB_URI)
                        .option("database", MONGODB_DATABASE)
                        .option("collection", "batch_reports")
                        .save();

                System.out.println("✅ Arama raporu MongoDB'ye kaydedildi.");
            }

            // 4. Bölge Özet Raporu
            System.out.println("\n" + "=".repeat(60));
            System.out.println("🗺️ BÖLGE ÖZET RAPORU");
            System.out.println("=".repeat(60));

            if (regionCount > 0) {
                // Tüm zamanların en aktif bölgeleri
                Dataset<Row> topRegionsAllTime = regionStats
                        .groupBy("region")
                        .agg(functions.sum("count").as("total_count"))
                        .orderBy(functions.desc("total_count"))
                        .limit(10);

                System.out.println("\n🏆 Tüm Zamanların En Aktif 10 Bölgesi:");
                topRegionsAllTime.show(false);

                // Toplam farklı bölge sayısı
                long uniqueRegions = regionStats.select("region").distinct().count();
                System.out.println("📍 Toplam Farklı Bölge: " + uniqueRegions);

                // Sonuçları batch_reports collection'ına kaydet
                Dataset<Row> regionReport = topRegionsAllTime
                        .withColumn("report_type", lit("all_time_top_regions"))
                        .withColumn("generated_at", current_timestamp());

                regionReport.write()
                        .format("mongodb")
                        .mode(SaveMode.Append)
                        .option("connection.uri", MONGODB_URI)
                        .option("database", MONGODB_DATABASE)
                        .option("collection", "batch_reports")
                        .save();

                System.out.println("✅ Bölge raporu MongoDB'ye kaydedildi.");
            }

            // 5. Genel Özet
            System.out.println("\n" + "=".repeat(60));
            System.out.println("📋 GENEL ÖZET");
            System.out.println("=".repeat(60));

            if (searchCount > 0) {
                // Toplam arama sayısı
                Dataset<Row> totalSearchCount = searchStats.agg(functions.sum("count").as("total"));
                Row totalRow = totalSearchCount.first();
                long total = totalRow.isNullAt(0) ? 0 : totalRow.getLong(0);
                System.out.println("🔍 Toplam Arama İşlemi: " + total);
            }

            if (regionCount > 0) {
                // Toplam bölge işlemi
                Dataset<Row> totalRegionCount = regionStats.agg(functions.sum("count").as("total"));
                Row totalRow = totalRegionCount.first();
                long total = totalRow.isNullAt(0) ? 0 : totalRow.getLong(0);
                System.out.println("📍 Toplam Bölge İşlemi: " + total);
            }

            // Batch sayısı
            if (searchCount > 0) {
                long batchCount = searchStats.select("batch_id").distinct().count();
                System.out.println("📦 Toplam Batch Sayısı: " + batchCount);
            }

            System.out.println("\n" + "=".repeat(60));
            System.out.println("✅ Batch analizi tamamlandı!");
            System.out.println("=".repeat(60));

        } catch (Exception e) {
            System.err.println("❌ Batch işleme hatası: " + e.getMessage());
            e.printStackTrace();
        } finally {
            spark.stop();
        }
    }
}
