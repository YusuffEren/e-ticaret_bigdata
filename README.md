# 🛒 E-Ticaret Gerçek Zamanlı Arama Analiz Sistemi

<p align="center">
  <img src="https://img.shields.io/badge/Java-17%20%7C%2021-orange?style=for-the-badge&logo=openjdk" alt="Java">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.0-green?style=for-the-badge&logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Apache%20Kafka-3.6.0-black?style=for-the-badge&logo=apachekafka" alt="Kafka">
  <img src="https://img.shields.io/badge/Apache%20Spark-3.5.0-orange?style=for-the-badge&logo=apachespark" alt="Spark">
  <img src="https://img.shields.io/badge/MongoDB-7.0-green?style=for-the-badge&logo=mongodb" alt="MongoDB">
  <img src="https://img.shields.io/badge/Docker-Ready-blue?style=for-the-badge&logo=docker" alt="Docker">
</p>

<p align="center">
  Apache Kafka, Spark Streaming ve MongoDB kullanarak e-ticaret platformlarındaki arama davranışlarını gerçek zamanlı analiz eden bir Big Data projesi.
</p>

---

## 📋 İçindekiler

- [Proje Hakkında](#-proje-hakkında)
- [Yeni Özellikler](#-yeni-özellikler---zaman-serisi-analizi)
- [Mimari](#-mimari)
- [Teknolojiler](#-teknolojiler)
- [Gereksinimler](#-gereksinimler)
- [Kurulum](#-kurulum)
- [Çalıştırma](#-çalıştırma)
- [Ekran Görüntüleri](#-ekran-görüntüleri)
- [API Dokümantasyonu](#-api-dokümantasyonu)
- [Proje Yapısı](#-proje-yapısı)

---

## 🎯 Proje Hakkında

Bu proje, e-ticaret platformlarındaki kullanıcı arama davranışlarını simüle ederek **gerçek zamanlı analiz** yapar.

### ✨ Özellikler

- 🔍 **Gerçek Zamanlı Arama Analizi** - Hangi ürünler en çok aranıyor?
- 🗺️ **Bölgesel Analiz** - Hangi şehirlerden arama yapılıyor?
- 📊 **Canlı Dashboard** - Chart.js ile görselleştirme
- ⏰ **Zaman Serisi Analizi** - Saatlik ve günlük trendler (YENİ!)
- 🚀 **Simülasyon Modu** - Otomatik veri üretimi
- 💾 **Kalıcı Depolama** - MongoDB ile veri saklama
- 📈 **Batch Analizi** - Geçmiş verilerin analizi
- 🔄 **Otomatik Yenileme** - 5 saniyede bir güncellenen grafikler

---

## 🆕 Yeni Özellikler - Zaman Serisi Analizi

### 📈 Saatlik Trend Grafiği
- Son 24 saatin saatlik arama dağılımını gösterir
- X ekseni: 00:00 - 23:00 saatleri
- Y ekseni: Her saatteki toplam arama sayısı
- Gerçek zamanlı güncelleme (5 saniyede bir)

### 📅 Günlük Trend Grafiği
- Son 7 günün günlük arama dağılımını gösterir
- X ekseni: Son 7 gün (örn: "14 Ara", "15 Ara")
- Y ekseni: Her gündeki toplam arama sayısı
- Gerçek zamanlı güncelleme (5 saniyede bir)

### Yeni API Endpoint'leri

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| `GET` | `/api/stats/hourly` | Son 24 saatin saatlik dağılımı |
| `GET` | `/api/stats/daily` | Son 7 günün günlük dağılımı |

### Örnek Yanıtlar

**Saatlik Veri:**
```json
{
  "status": "success",
  "data": [
    {"hour": "00:00", "count": 45},
    {"hour": "01:00", "count": 32},
    {"hour": "14:00", "count": 128}
  ],
  "lastUpdated": 1702567890123
}
```

**Günlük Veri:**
```json
{
  "status": "success",
  "data": [
    {"date": "2025-12-08", "count": 1250},
    {"date": "2025-12-14", "count": 2340}
  ],
  "lastUpdated": 1702567890123
}
```

---

## 🏗 Mimari

```
                                    ┌─────────────────┐
                                    │    FRONTEND     │
                                    │  (HTML/JS/CSS)  │
                                    │                 │
                                    │ ┌─────────────┐ │
                                    │ │ Bar Chart   │ │
                                    │ │ Doughnut    │ │
                                    │ │ Line Charts │ │ ◄── YENİ!
                                    │ └─────────────┘ │
                                    └────────┬────────┘
                                             │
                                             ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         E-TİCARET API                                    │
│                      (Spring Boot 3.2.0)                                │
│                                                                          │
│   POST /api/search ──────► Kafka Producer ──────► search-analysisv2     │
│   GET /api/stats/* ◄────── MongoDB                                      │
│   GET /api/stats/hourly ◄── time_stats (YENİ!)                          │
│   GET /api/stats/daily ◄─── time_stats (YENİ!)                          │
└─────────────────────────────────────────────────────────────────────────┘
                                             │
                                             ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         APACHE KAFKA                                     │
│                                                                          │
│                    Topic: search-analysisv2                             │
└─────────────────────────────────────────────────────────────────────────┘
                                             │
                           ┌─────────────────┴─────────────────┐
                           ▼                                   ▼
            ┌──────────────────────────┐       ┌──────────────────────────┐
            │    SPARK STREAMING       │       │      SPARK BATCH         │
            │    (Consumer)            │       │      (Analiz)            │
            │                          │       │                          │
            │  Kafka ─► Aggregation    │       │  MongoDB ─► Raporlama    │
            │        ─► Time Stats     │ ◄── YENİ!                        │
            │           │              │       │                          │
            └───────────┼──────────────┘       └──────────────────────────┘
                        │                                   │
                        ▼                                   ▼
            ┌─────────────────────────────────────────────────────────────┐
            │                        MONGODB                               │
            │                                                              │
            │ search_stats │ region_stats │ time_stats │ batch_reports    │
            │              │              │   (YENİ!)  │                  │
            └─────────────────────────────────────────────────────────────┘
```

---

## 🛠 Teknolojiler

| Teknoloji | Versiyon | Kullanım |
|-----------|----------|----------|
| **Java** | 17+ | Backend geliştirme |
| **Spring Boot** | 3.2.0 | REST API |
| **Apache Kafka** | 3.6.0 | Mesaj kuyruğu |
| **Apache Spark** | 3.5.0 | Stream & Batch processing |
| **MongoDB** | 7.0 | NoSQL veritabanı |
| **Chart.js** | 4.x | Dashboard grafikleri (Bar, Doughnut, Line) |
| **Docker** | - | Container yönetimi |

---

## 📦 Gereksinimler

- ☕ **Java 17+** (Spark için Java 17 önerilir)
- 🐳 **Docker Desktop**
- 📦 **Maven 3.6+** (veya Maven Wrapper)
- 🪟 **Windows için:** Hadoop winutils.exe ve hadoop.dll

### Windows için Hadoop Kurulumu

1. `C:\hadoop\bin` klasörü oluşturun
2. [winutils.exe](https://github.com/cdarlint/winutils) dosyasını `C:\hadoop\bin\` klasörüne kopyalayın
3. [hadoop.dll](https://github.com/cdarlint/winutils) dosyasını `C:\hadoop\bin\` klasörüne kopyalayın
4. Ortam değişkeni ayarlayın:
   ```cmd
   setx HADOOP_HOME "C:\hadoop"
   ```

---

## 🚀 Kurulum

### 1. Projeyi Klonlayın

```bash
git clone https://github.com/YusuffEren/e-ticaret_bigdata.git
cd e-ticaret_bigdata
```

### 2. Docker Altyapısını Başlatın

```bash
cd eticaret-api
docker-compose up -d
```

Bu komut şunları başlatır:
- 🦓 **Zookeeper** → localhost:2181
- 📨 **Kafka** → localhost:9092
- 🖥️ **Kafka UI** → http://localhost:8080
- 🍃 **MongoDB** → localhost:27017
- 📊 **Mongo Express** → http://localhost:8082

---

## ▶️ Çalıştırma

### Terminal 1 - API

```bash
cd eticaret-api
.\mvnw.cmd spring-boot:run
```
> API: http://localhost:8081

### Terminal 2 - Spark Consumer

```powershell
cd eticaret-consumer
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;C:\hadoop\bin;$env:PATH"
$env:HADOOP_HOME = "C:\hadoop"
$env:MAVEN_OPTS = "--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/sun.nio.cs=ALL-UNNAMED --add-opens=java.base/sun.security.action=ALL-UNNAMED --add-opens=java.base/sun.util.calendar=ALL-UNNAMED"
.\mvnw.cmd exec:java
```

### Frontend

Tarayıcıda açın:
- **Ana Sayfa:** `eticaret-frontend/index.html`
- **Dashboard:** `eticaret-frontend/dashboard.html`

### Simülasyonu Başlatın

Ana sayfadaki "Simülasyonu Başlat" butonuna tıklayın veya:
```bash
curl http://localhost:8081/api/search
```

---

## 📸 Ekran Görüntüleri

### Dashboard
Dashboard şu bileşenleri içerir:

| Grafik | Açıklama |
|--------|----------|
| 📊 **En Çok Aranan Ürünler** | Bar chart - Top 10 arama terimi |
| 🍩 **Bölgelere Göre Dağılım** | Doughnut chart - Şehir bazlı dağılım |
| 📈 **Saatlik Trend** | Line chart - Son 24 saat (YENİ!) |
| 📅 **Günlük Trend** | Line chart - Son 7 gün (YENİ!) |

### Spark Console
- Streaming analiz sonuçları
- MongoDB yazma durumu
- Zaman bazlı istatistikler (YENİ!)

---

## 📡 API Dokümantasyonu

### Endpoints

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| `GET` | `/api/search` | Simülasyonu başlat |
| `GET` | `/api/stop` | Simülasyonu durdur |
| `GET` | `/api/status` | Sistem durumu |
| `GET` | `/api/stats/searches` | En çok arananlar |
| `GET` | `/api/stats/regions` | Bölge dağılımı |
| `GET` | `/api/stats/dashboard` | Dashboard verileri |
| `GET` | `/api/stats/summary` | Özet istatistikler |
| `GET` | `/api/stats/hourly` | Saatlik trend (YENİ!) |
| `GET` | `/api/stats/daily` | Günlük trend (YENİ!) |

### Örnek Yanıt - Dashboard

```json
{
  "status": "success",
  "topSearches": [
    {"name": "telefon", "value": 150},
    {"name": "laptop", "value": 120}
  ],
  "regionDistribution": [
    {"name": "İstanbul", "value": 200},
    {"name": "Ankara", "value": 150}
  ],
  "lastUpdated": 1702567890123
}
```

---

## 📁 Proje Yapısı

```
e-ticaret_bigdata/
│
├── eticaret-api/                    # Spring Boot API (Producer)
│   ├── src/main/java/
│   │   └── com/bigdatacompany/eticaret/
│   │       ├── Application.java
│   │       ├── MessageProducer.java
│   │       ├── api/
│   │       │   ├── SearchController.java
│   │       │   └── StatsController.java      # hourly/daily endpoints
│   │       ├── config/
│   │       │   └── CorsConfig.java
│   │       ├── model/
│   │       │   ├── SearchStat.java
│   │       │   ├── RegionStat.java
│   │       │   └── TimeStat.java             # YENİ!
│   │       └── repository/
│   │           ├── SearchStatRepository.java
│   │           ├── RegionStatRepository.java
│   │           └── TimeStatRepository.java   # YENİ!
│   ├── docker-compose.yml
│   └── pom.xml
│
├── eticaret-consumer/               # Spark Streaming (Consumer)
│   ├── src/main/java/
│   │   └── com/bigdatacompany/eticaret/consumer/
│   │       ├── SparkConsumerApplication.java # time_stats desteği
│   │       └── SparkBatchApplication.java
│   └── pom.xml
│
├── eticaret-frontend/               # Web Arayüzü
│   ├── index.html
│   ├── dashboard.html               # Zaman grafikleri eklendi
│   ├── css/style.css                # Yeni stiller
│   └── js/
│       ├── app.js
│       └── dashboard.js             # Line chart implementasyonu
│
└── README.md
```

---

## 🐳 Docker Servisleri

| Servis | Port | URL |
|--------|------|-----|
| Kafka | 9092 | - |
| Kafka UI | 8080 | http://localhost:8080 |
| MongoDB | 27017 | - |
| Mongo Express | 8082 | http://localhost:8082 |
| Zookeeper | 2181 | - |

**MongoDB Credentials:** admin / admin123

---

## 📊 MongoDB Koleksiyonları

| Koleksiyon | Açıklama |
|------------|----------|
| `search_stats` | Arama terimi istatistikleri |
| `region_stats` | Bölge bazlı istatistikler |
| `time_stats` | Saatlik/günlük zaman istatistikleri (YENİ!) |

### time_stats Veri Yapısı

```json
{
  "hour": 14,
  "date": "2025-12-14",
  "count": 128,
  "batch_id": 42,
  "updated_at": "2025-12-14T14:30:00Z"
}
```

---

## 📊 Kafka Mesaj Formatı

```json
{
  "search": "telefon",
  "region": "İstanbul",
  "current_ts": "2024-01-15 14:30:00",
  "timestamp": "1705326600000"
}
```

---

## 🔧 Sorun Giderme

| Sorun | Çözüm |
|-------|-------|
| Kafka bağlantı hatası | Docker çalışıyor mu? `docker ps` |
| Java module hatası | MAVEN_OPTS'u ayarladın mı? |
| Hadoop hatası | HADOOP_HOME ayarlandı mı? winutils.exe ve hadoop.dll var mı? |
| Port meşgul | `netstat -ano \| findstr :PORT` ile kontrol et |
| Grafikler güncellenmiyor | API ve Consumer çalışıyor mu kontrol et |
| Zaman grafikleri boş | Simülasyonu başlat ve birkaç saniye bekle |

---

## 📝 Değişiklik Geçmişi

### v2.0.0 (2025-12-14)
- ✨ Zaman Serisi Analizi özelliği eklendi
- 📈 Saatlik trend grafiği (Line Chart)
- 📅 Günlük trend grafiği (Line Chart)
- 🆕 `/api/stats/hourly` endpoint'i
- 🆕 `/api/stats/daily` endpoint'i
- 🆕 `time_stats` MongoDB koleksiyonu
- 🆕 `TimeStat` model sınıfı
- 🔄 Dashboard 5 saniyede bir otomatik güncelleme

### v1.0.0
- 🚀 İlk sürüm
- 🔍 Gerçek zamanlı arama analizi
- 🗺️ Bölgesel analiz
- 📊 Dashboard grafikleri

---

## 👨‍💻 Geliştirici

**Yusuf Eren** - [GitHub](https://github.com/YusuffEren)

---

## 📄 Lisans

Bu proje eğitim amaçlı geliştirilmiştir.
