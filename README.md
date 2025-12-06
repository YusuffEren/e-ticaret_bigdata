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
- 🚀 **Simülasyon Modu** - Otomatik veri üretimi
- 💾 **Kalıcı Depolama** - MongoDB ile veri saklama
- 📈 **Batch Analizi** - Geçmiş verilerin analizi

---

## 🏗 Mimari

```
                                    ┌─────────────────┐
                                    │    FRONTEND     │
                                    │  (HTML/JS/CSS)  │
                                    └────────┬────────┘
                                             │
                                             ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         E-TİCARET API                                    │
│                      (Spring Boot 3.2.0)                                │
│                                                                          │
│   POST /api/search ──────► Kafka Producer ──────► search-analysisv2     │
│   GET /api/stats/* ◄────── MongoDB                                      │
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
            │           │              │       │                          │
            └───────────┼──────────────┘       └──────────────────────────┘
                        │                                   │
                        ▼                                   ▼
            ┌─────────────────────────────────────────────────────────────┐
            │                        MONGODB                               │
            │                                                              │
            │   search_stats   │   region_stats   │   batch_reports       │
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
| **Chart.js** | 4.x | Dashboard grafikleri |
| **Docker** | - | Container yönetimi |

---

## 📦 Gereksinimler

- ☕ **Java 17+** (Spark için Java 17 önerilir)
- 🐳 **Docker Desktop**
- 📦 **Maven 3.6+** (veya Maven Wrapper)
- 🪟 **Windows için:** Hadoop winutils.exe

### Windows için Hadoop Kurulumu

1. [Hadoop 3.4.1](https://hadoop.apache.org/releases.html) indirin
2. `C:\hadoop-3.4.1` klasörüne çıkarın
3. [winutils.exe](https://github.com/cdarlint/winutils) dosyasını `C:\hadoop-3.4.1\bin\` klasörüne kopyalayın
4. Ortam değişkeni ayarlayın:
   ```cmd
   setx HADOOP_HOME "C:\hadoop-3.4.1"
   ```

---

## 🚀 Kurulum

### 1. Projeyi Klonlayın

```bash
git clone https://github.com/kullaniciadi/eticaret-bigdata.git
cd eticaret-bigdata
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

```bash
cd eticaret-consumer
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot
set HADOOP_HOME=C:\hadoop-3.4.1
set PATH=%HADOOP_HOME%\bin;%PATH%
set MAVEN_OPTS=--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/sun.nio.cs=ALL-UNNAMED --add-opens=java.base/sun.security.action=ALL-UNNAMED --add-opens=java.base/sun.util.calendar=ALL-UNNAMED
.\mvnw.cmd exec:java
```

### Frontend

Tarayıcıda açın:
- **Ana Sayfa:** `eticaret-frontend/index.html`
- **Dashboard:** `eticaret-frontend/dashboard.html`

### Simülasyonu Başlatın

```bash
curl http://localhost:8081/api/search
```

---

## 📸 Ekran Görüntüleri

### Ana Sayfa
Modern arama arayüzü, hızlı arama etiketleri ve simülasyon kontrolleri.

### Dashboard
Gerçek zamanlı grafikler, en çok aranan ürünler ve bölge dağılımı.

### Spark Console
Streaming analiz sonuçları ve MongoDB yazma durumu.

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

### Örnek Yanıt

```json
{
  "status": "success",
  "topSearches": [
    {"search": "telefon", "count": 150},
    {"search": "laptop", "count": 120}
  ],
  "regionDistribution": [
    {"region": "İstanbul", "count": 200},
    {"region": "Ankara", "count": 150}
  ]
}
```

---

## 📁 Proje Yapısı

```
eticaret-bigdata/
│
├── eticaret-api/                    # Spring Boot API (Producer)
│   ├── src/main/java/
│   │   └── com/bigdatacompany/eticaret/
│   │       ├── Application.java
│   │       ├── MessageProducer.java
│   │       ├── api/
│   │       │   ├── SearchController.java
│   │       │   └── StatsController.java
│   │       ├── config/
│   │       │   └── CorsConfig.java
│   │       ├── model/
│   │       └── repository/
│   ├── docker-compose.yml
│   └── pom.xml
│
├── eticaret-consumer/               # Spark Streaming (Consumer)
│   ├── src/main/java/
│   │   └── com/bigdatacompany/eticaret/consumer/
│   │       ├── SparkConsumerApplication.java
│   │       └── SparkBatchApplication.java
│   └── pom.xml
│
├── eticaret-frontend/               # Web Arayüzü
│   ├── index.html
│   ├── dashboard.html
│   ├── css/style.css
│   └── js/
│       ├── app.js
│       └── dashboard.js
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
| Hadoop hatası | HADOOP_HOME ayarlandı mı? winutils.exe var mı? |
| Port meşgul | `netstat -ano \| findstr :PORT` ile kontrol et |

</p>

