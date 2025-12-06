# E-Ticaret Arama Kutusu Frontend

Modern, responsive web arayüzü - E-Ticaret Arama Analiz Sistemi için.

## 🚀 Özellikler

- 🔍 **Arama Sayfası** - Ürün arama ve Kafka'ya veri gönderme
- 📊 **Dashboard** - Chart.js ile gerçek zamanlı istatistikler
- 🎨 **Modern Tasarım** - Glassmorphism, gradientler ve animasyonlar
- 📱 **Responsive** - Mobil uyumlu tasarım

## 📁 Dosya Yapısı

```
eticaret-frontend/
├── index.html       # Ana arama sayfası
├── dashboard.html   # İstatistik dashboard'u
├── css/
│   └── style.css    # Stil dosyası
├── js/
│   ├── app.js       # Ana uygulama JS
│   └── dashboard.js # Dashboard JS
└── README.md
```

## 🛠 Kullanım

### 1. API'nin Çalıştığından Emin Olun

```bash
cd eticaret-api
mvnw.cmd spring-boot:run
```

API `http://localhost:8081` adresinde çalışmalı.

### 2. Frontend'i Açın

`index.html` dosyasını tarayıcıda açın:

```bash
# Windows'ta
start index.html

# Veya doğrudan tarayıcıda açın
```

### 3. Live Server (Önerilen)

VS Code Live Server veya benzeri bir araç kullanın:

```bash
# Node.js http-server ile
npx http-server -p 3000

# Sonra tarayıcıda açın: http://localhost:3000
```

## 📡 API Endpoints

Frontend şu API endpoint'lerini kullanır:

| Endpoint | Açıklama |
|----------|----------|
| `GET /api/status` | Sistem durumu |
| `GET /api/search` | Simülasyon başlat |
| `GET /api/stop` | Simülasyon durdur |
| `GET /api/stats/dashboard` | Dashboard verileri |
| `GET /api/stats/summary` | Özet istatistikler |
| `GET /api/stats/searches` | En çok aranan ürünler |
| `GET /api/stats/regions` | Bölge dağılımı |

## 🎨 Tasarım

- **Renk Paleti**: Mor, pembe, cyan gradientler
- **Font**: Inter (Google Fonts)
- **İkonlar**: Font Awesome 6
- **Grafikler**: Chart.js

## 📊 Dashboard Özellikleri

- Otomatik yenileme (5 saniye)
- Bar chart - En çok aranan ürünler
- Doughnut chart - Bölge dağılımı
- Detaylı tablolar
- Gerçek zamanlı bağlantı durumu

---

Made with ❤️ for Big Data Course
