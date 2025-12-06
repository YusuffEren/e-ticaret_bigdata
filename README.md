# 🛒 E-Ticaret Gerçek Zamanlı Arama Analiz Sistemi

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.0-green?style=for-the-badge&logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Apache%20Kafka-3.6.0-black?style=for-the-badge&logo=apachekafka" alt="Kafka">
  <img src="https://img.shields.io/badge/Apache%20Spark-3.5.0-orange?style=for-the-badge&logo=apachespark" alt="Spark">
  <img src="https://img.shields.io/badge/Docker-Ready-blue?style=for-the-badge&logo=docker" alt="Docker">
</p>

Apache Kafka ve Spark Structured Streaming kullanarak e-ticaret platformlarındaki arama davranışlarını gerçek zamanlı analiz eden bir Big Data projesi.

---

## 📋 İçindekiler

- [Proje Hakkında](#-proje-hakkında)
- [Mimari](#-mimari)
- [Teknolojiler](#-teknolojiler)
- [Kurulum](#-kurulum)
- [Kullanım](#-kullanım)
- [API Dokümantasyonu](#-api-dokümantasyonu)
- [Ekran Görüntüleri](#-ekran-görüntüleri)
- [Katkıda Bulunma](#-katkıda-bulunma)

---

## 🎯 Proje Hakkında

Bu proje, e-ticaret platformlarındaki kullanıcı arama davranışlarını simüle ederek gerçek zamanlı analiz yapar. Sistem iki ana bileşenden oluşur:

- **Producer (API)**: Kullanıcı aramalarını simüle eder ve Kafka'ya gönderir
- **Consumer (Spark)**: Kafka'dan verileri okur ve gerçek zamanlı istatistikler üretir

### Kullanım Alanları
- 📊 Trend analizi
- 🔍 En çok aranan ürünlerin tespiti
- 🗺️ Bölgesel arama eğilimleri
- 📈 Gerçek zamanlı dashboard'lar

---

## 🏗 Mimari

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           KULLANICI                                     │
│                              │                                          │
│                              ▼                                          │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                    E-TİCARET API (Producer)                       │  │
│  │                    Spring Boot 3.2.0 - Java 21                    │  │
│  │                                                                   │  │
│  │   /api/search ──────▶ Simüle Veri Üret ──────▶ Kafka Producer    │  │
│  │   /api/stop                                                       │  │
│  │   /api/status                                                     │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                              │                                          │
│                              ▼                                          │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                     APACHE KAFKA                                  │  │
│  │                Topic: search-analysisv2                           │  │
│  │   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐               │  │
│  │   │ Partition 0 │  │ Partition 1 │  │ Partition 2 │               │  │
│  │   └─────────────┘  └─────────────┘  └─────────────┘               │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                              │                                          │
│                              ▼                                          │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                 SPARK STREAMING (Consumer)                        │  │
│  │                 Apache Spark 3.5.0 - Java 17                      │  │
│  │                                                                   │  │
│  │   Kafka Source ──▶ JSON Parse ──▶ Aggregation ──▶ Console Output  │  │
│  │                                                                   │  │
│  │   • En çok aranan ürünler                                         │  │
│  │   • Bölgesel arama dağılımı                                       │  │
│  │   • Zaman bazlı analiz                                            │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 🛠 Teknolojiler

| Teknoloji | Versiyon | Kullanım Amacı |
|-----------|----------|----------------|
| Java | 21 | API geliştirme |
| Java | 17 | Spark uygulaması |
| Spring Boot | 3.2.0 | REST API framework |
| Apache Kafka | 3.6.0 | Mesaj kuyruğu |
| Apache Spark | 3.5.0 | Stream processing |
| Docker | - | Altyapı yönetimi |
| Zookeeper | 7.4.0 | Kafka koordinasyonu |
| Maven | 3.9.6 | Bağımlılık yönetimi |

---

## 📦 Kurulum

### Gereksinimler

- ☕ Java 17+ (JDK)
- 🐳 Docker & Docker Compose
- 📦 Maven 3.6+
- 🪟 Windows için: Hadoop winutils.exe

### 1. Projeyi Klonlayın

```bash
git clone https://github.com/kullaniciadi/eticaret-analysis.git
cd eticaret-analysis
```

### 2. Kafka Altyapısını Başlatın

```bash
cd eticaret-api
docker-compose up -d
```

Bu komut şunları başlatır:
- 🦓 **Zookeeper** → `localhost:2181`
- 📨 **Kafka** → `localhost:9092`
- 🖥️ **Kafka UI** → `http://localhost:8080`

### 3. Windows için Hadoop Kurulumu

```powershell
# HADOOP_HOME environment variable ayarlayın
setx HADOOP_HOME "C:\hadoop-3.4.1"

# veya PowerShell ile
$env:HADOOP_HOME = "C:\hadoop-3.4.1"
```

> ⚠️ **Not**: `winutils.exe` dosyasını `%HADOOP_HOME%\bin` klasörüne yerleştirin.

---

## 🚀 Kullanım

### Adım 1: API'yi Başlatın

```bash
cd eticaret-api
mvnw.cmd spring-boot:run
```

API `http://localhost:8081` adresinde çalışmaya başlar.

### Adım 2: Spark Consumer'ı Başlatın

Yeni bir terminal açın:

```bash
cd eticaret-consumer
mvnw.cmd exec:java
```

### Adım 3: Veri Üretimini Başlatın

Tarayıcınızda veya curl ile:

```bash
# Varsayılan (1 saniye aralıkla)
curl http://localhost:8081/api/search

# 500ms aralıkla
curl "http://localhost:8081/api/search?intervalMs=500"
```

### Adım 4: Sonuçları İzleyin

Spark Consumer terminalinde gerçek zamanlı istatistikler görünecektir:

```
╔══════════════════════════════════════════╗
║  ARAMA İSTATİSTİKLERİ - Batch: 5         ║
╠══════════════════════════════════════════╣
║  Toplam farklı arama: 6                  ║
╚══════════════════════════════════════════╝

📊 En Çok Aranan Ürünler:
+----------+-----+
|search    |count|
+----------+-----+
|telefon   |25   |
|laptop    |18   |
|ayakkabı  |15   |
|kulaklık  |12   |
+----------+-----+
```

---

## 📡 API Dokümantasyonu

### Endpoints

| Method | Endpoint | Açıklama | Parametreler |
|--------|----------|----------|--------------|
| GET | `/api/search` | Veri üretimini başlat | `intervalMs` (opsiyonel, default: 1000) |
| GET | `/api/stop` | Veri üretimini durdur | - |
| GET | `/api/status` | Sistem durumunu göster | - |
| GET | `/actuator/health` | Sağlık kontrolü | - |

### Örnek Yanıtlar

**Veri Üretimi Başlat:**
```json
{
  "status": "success",
  "message": "Veri üretimi başlatıldı!",
  "interval": "1000ms",
  "stopEndpoint": "/api/stop"
}
```

**Durum Sorgula:**
```json
{
  "isRunning": true,
  "messageCount": 150,
  "cities": ["Ankara", "İstanbul", "Mersin", ...],
  "products": ["telefon", "laptop", "ayakkabı", ...]
}
```

### Kafka Mesaj Formatı

```json
{
  "search": "telefon",
  "region": "İstanbul",
  "current_ts": "2023-05-15 14:32:00",
  "timestamp": "2024-12-06 17:00:00"
}
```

---

## 📁 Proje Yapısı

```
├── eticaret-api/                    # Producer API
│   ├── src/main/java/
│   │   └── com/bigdatacompany/eticaret/
│   │       ├── Application.java           # Spring Boot başlatıcı
│   │       ├── MessageProducer.java       # Kafka producer
│   │       └── api/
│   │           └── SearchController.java  # REST endpoints
│   ├── src/main/resources/
│   │   └── application.properties         # Konfigürasyon
│   ├── docker-compose.yml                 # Kafka altyapısı
│   └── pom.xml
│
├── eticaret-consumer/               # Consumer (Spark)
│   ├── src/main/java/
│   │   └── com/bigdatacompany/eticaret/consumer/
│   │       └── SparkConsumerApplication.java  # Spark streaming
│   └── pom.xml
│
└── README.md
```

---

## 🔧 Konfigürasyon

### API (application.properties)

```properties
# Server
server.port=8081

# Kafka
kafka.bootstrap.servers=localhost:9092
kafka.topic=search-analysisv2

# Actuator
management.endpoints.web.exposure.include=health,info
```

### Consumer (SparkConsumerApplication.java)

```java
private static final String KAFKA_BOOTSTRAP_SERVERS = "localhost:9092";
private static final String KAFKA_TOPIC = "search-analysisv2";
```

---

## 🐳 Docker Servisleri

| Servis | Port | Açıklama |
|--------|------|----------|
| Zookeeper | 2181 | Kafka koordinatörü |
| Kafka | 9092, 29092 | Mesaj broker |
| Kafka UI | 8080 | Web arayüzü |

```bash
# Tüm servisleri başlat
docker-compose up -d

# Logları izle
docker-compose logs -f kafka

# Servisleri durdur
docker-compose down
```

---

## 📊 Simülasyon Verileri

### Şehirler
- Ankara, İstanbul, Mersin, Gaziantep, Samsun
- Ordu, İzmir, Bursa, Antalya, Adana

### Ürünler
- bebek bezi, telefon, televizyon, ayakkabı, havlu
- kitap, laptop, kulaklık, saat, çanta

---

## 🤝 Katkıda Bulunma

1. 🍴 Fork yapın
2. 🌿 Feature branch oluşturun (`git checkout -b feature/yeni-ozellik`)
3. 💾 Değişikliklerinizi commit edin (`git commit -m 'Yeni özellik ekle'`)
4. 📤 Branch'i push edin (`git push origin feature/yeni-ozellik`)
5. 🔄 Pull Request açın

---

## 📝 Lisans

Bu proje MIT Lisansı altında lisanslanmıştır.

---

## 👤 İletişim

Yusuf - [@github](https://github.com/yusuf)

Proje Linki: [https://github.com/yusuf/eticaret-analysis](https://github.com/yusuf/eticaret-analysis)

---

<p align="center">
  ⭐ Projeyi beğendiyseniz yıldız vermeyi unutmayın!
</p>
