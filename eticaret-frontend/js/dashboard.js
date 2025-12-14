/**
 * E-Ticaret Analytics - Dashboard JS
 * Dashboard görselleştirme ve veri yönetimi
 */

// API Base URL
const API_BASE = 'http://localhost:8081/api';

// Chart instances
let searchChart = null;
let regionChart = null;
let hourlyChart = null;
let dailyChart = null;

// Auto refresh interval
let autoRefreshInterval = null;

// DOM Elements
const refreshBtn = document.getElementById('refresh-data');
const autoRefreshCheckbox = document.getElementById('auto-refresh');
const connectionStatus = document.getElementById('connection-status');
const totalSearchesEl = document.getElementById('total-searches');
const totalRegionsEl = document.getElementById('total-regions');
const lastUpdateEl = document.getElementById('last-update');
const dbStatusEl = document.getElementById('db-status');
const searchTableBody = document.getElementById('search-table-body');
const regionTableBody = document.getElementById('region-table-body');

// Chart colors
const chartColors = [
    '#6366f1', '#8b5cf6', '#ec4899', '#f43f5e', '#f97316',
    '#eab308', '#84cc16', '#22c55e', '#14b8a6', '#06b6d4'
];

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    initializeCharts();
    initializeTimeCharts();
    setupEventListeners();
    loadDashboardData();
    startAutoRefresh();
});

/**
 * Initialize Chart.js charts
 */
function initializeCharts() {
    // Search Chart - Bar Chart
    const searchCtx = document.getElementById('searchChart').getContext('2d');
    searchChart = new Chart(searchCtx, {
        type: 'bar',
        data: {
            labels: [],
            datasets: [{
                label: 'Arama Sayısı',
                data: [],
                backgroundColor: chartColors.map(c => c + '99'),
                borderColor: chartColors,
                borderWidth: 2,
                borderRadius: 8,
                borderSkipped: false
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            animation: {
                duration: 500,
                easing: 'easeOutQuart'
            },
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    titleColor: '#fff',
                    bodyColor: '#fff',
                    padding: 12,
                    borderColor: 'rgba(255, 255, 255, 0.1)',
                    borderWidth: 1,
                    displayColors: false,
                    callbacks: {
                        label: function (context) {
                            return `${context.parsed.y} arama`;
                        }
                    }
                }
            },
            scales: {
                x: {
                    grid: {
                        display: false
                    },
                    ticks: {
                        color: '#a5b4fc',
                        maxRotation: 45,
                        minRotation: 0
                    }
                },
                y: {
                    beginAtZero: true,
                    grace: '10%',
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)'
                    },
                    ticks: {
                        color: '#a5b4fc',
                        stepSize: 1,
                        callback: function(value) {
                            if (Number.isInteger(value)) {
                                return value;
                            }
                        }
                    }
                }
            }
        }
    });

    // Region Chart - Doughnut Chart
    const regionCtx = document.getElementById('regionChart').getContext('2d');
    regionChart = new Chart(regionCtx, {
        type: 'doughnut',
        data: {
            labels: [],
            datasets: [{
                data: [],
                backgroundColor: chartColors,
                borderColor: '#0f0f23',
                borderWidth: 2,
                hoverOffset: 10
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            animation: {
                duration: 500,
                easing: 'easeOutQuart'
            },
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    titleColor: '#fff',
                    bodyColor: '#fff',
                    padding: 12,
                    borderColor: 'rgba(255, 255, 255, 0.1)',
                    borderWidth: 1,
                    callbacks: {
                        label: function (context) {
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = ((context.parsed / total) * 100).toFixed(1);
                            return `${context.label}: ${context.parsed} (${percentage}%)`;
                        }
                    }
                }
            },
            cutout: '60%'
        }
    });
}

/**
 * Setup event listeners
 */
function setupEventListeners() {
    // Refresh button
    refreshBtn.addEventListener('click', () => {
        refreshBtn.querySelector('i').classList.add('fa-spin');
        loadDashboardData().finally(() => {
            setTimeout(() => {
                refreshBtn.querySelector('i').classList.remove('fa-spin');
            }, 500);
        });
    });

    // Auto refresh toggle
    autoRefreshCheckbox.addEventListener('change', (e) => {
        if (e.target.checked) {
            startAutoRefresh();
        } else {
            stopAutoRefresh();
        }
    });
}

/**
 * Start auto refresh
 */
function startAutoRefresh() {
    if (autoRefreshInterval) {
        clearInterval(autoRefreshInterval);
    }
    autoRefreshInterval = setInterval(loadDashboardData, 5000);
}

/**
 * Stop auto refresh
 */
function stopAutoRefresh() {
    if (autoRefreshInterval) {
        clearInterval(autoRefreshInterval);
        autoRefreshInterval = null;
    }
}

/**
 * Load all dashboard data
 */
async function loadDashboardData() {
    await Promise.all([
        loadSummary(),
        loadDashboard(),
        loadHourlyData(),
        loadDailyData()
    ]);
}

/**
 * Load summary statistics
 */
async function loadSummary() {
    try {
        const response = await fetch(`${API_BASE}/stats/summary`);
        const data = await response.json();

        if (data.status === 'success') {
            // Toplam arama sayısı (tüm aramaların toplamı)
            totalSearchesEl.textContent = formatNumber(data.totalSearchRecords || 0);
            // Farklı bölge sayısı
            totalRegionsEl.textContent = formatNumber(data.totalRegionRecords || 0);
            dbStatusEl.textContent = data.collectionsActive ? 'Aktif' : 'Boş';
            dbStatusEl.style.color = data.collectionsActive ? '#10b981' : '#f59e0b';

            // Update connection status
            updateConnectionStatus(true);
        }
    } catch (error) {
        console.error('Summary fetch error:', error);
        updateConnectionStatus(false);
        totalSearchesEl.textContent = '-';
        totalRegionsEl.textContent = '-';
        dbStatusEl.textContent = 'Hata';
        dbStatusEl.style.color = '#ef4444';
    }
}

/**
 * Load dashboard data (charts and tables)
 */
async function loadDashboard() {
    try {
        const response = await fetch(`${API_BASE}/stats/dashboard`);
        const data = await response.json();

        if (data.status === 'success') {
            // Update charts
            updateSearchChart(data.topSearches || []);
            updateRegionChart(data.regionDistribution || []);

            // Update tables
            updateSearchTable(data.topSearches || []);
            updateRegionTable(data.regionDistribution || []);

            // Update last update time
            lastUpdateEl.textContent = new Date().toLocaleTimeString('tr-TR');

            // Update connection status
            updateConnectionStatus(true);
        }
    } catch (error) {
        console.error('Dashboard fetch error:', error);
        updateConnectionStatus(false);
        lastUpdateEl.textContent = 'Hata';
    }
}

/**
 * Update search bar chart
 */
function updateSearchChart(data) {
    // Filter out null/empty values
    const validData = data.filter(d => d.name && d.value > 0);
    
    if (!validData.length) {
        searchChart.data.labels = ['Veri Bekleniyor'];
        searchChart.data.datasets[0].data = [0];
        searchChart.data.datasets[0].backgroundColor = ['rgba(100, 100, 100, 0.5)'];
        searchChart.data.datasets[0].borderColor = ['#666'];
    } else {
        searchChart.data.labels = validData.map(d => d.name);
        searchChart.data.datasets[0].data = validData.map(d => d.value);
        searchChart.data.datasets[0].backgroundColor = chartColors.slice(0, validData.length).map(c => c + '99');
        searchChart.data.datasets[0].borderColor = chartColors.slice(0, validData.length);
    }
    searchChart.update();

    // Update legend
    updateChartLegend('search-legend', validData, searchChart);
}

/**
 * Update region doughnut chart
 */
function updateRegionChart(data) {
    // Filter out null/empty values
    const validData = data.filter(d => d.name && d.value > 0);
    
    if (!validData.length) {
        regionChart.data.labels = ['Veri Bekleniyor'];
        regionChart.data.datasets[0].data = [1];
        regionChart.data.datasets[0].backgroundColor = ['rgba(100, 100, 100, 0.5)'];
    } else {
        regionChart.data.labels = validData.map(d => d.name);
        regionChart.data.datasets[0].data = validData.map(d => d.value);
        regionChart.data.datasets[0].backgroundColor = chartColors.slice(0, validData.length);
    }
    regionChart.update();

    // Update legend
    updateChartLegend('region-legend', validData, regionChart);
}

/**
 * Update chart legend
 */
function updateChartLegend(elementId, data, chart) {
    const legendEl = document.getElementById(elementId);
    if (!data.length) {
        legendEl.innerHTML = '<span style="color: #6b7280;">Henüz veri yok</span>';
        return;
    }

    legendEl.innerHTML = data.slice(0, 5).map((item, index) => `
        <span class="legend-item" style="
            display: inline-flex;
            align-items: center;
            gap: 4px;
            padding: 4px 8px;
            background: rgba(255,255,255,0.05);
            border-radius: 4px;
            font-size: 0.75rem;
        ">
            <span style="
                width: 10px;
                height: 10px;
                border-radius: 2px;
                background: ${chartColors[index % chartColors.length]};
            "></span>
            ${item.name || 'Bilinmiyor'}
        </span>
    `).join('');
}

/**
 * Update search table
 */
function updateSearchTable(data) {
    // Filter out null/empty values
    const validData = data.filter(d => d.name && d.value > 0);
    
    if (!validData.length) {
        searchTableBody.innerHTML = `
            <tr>
                <td colspan="4" class="empty-state">Henüz arama verisi yok - Simülasyonu başlatın</td>
            </tr>
        `;
        return;
    }

    const total = validData.reduce((sum, item) => sum + (item.value || 0), 0);

    searchTableBody.innerHTML = validData.map((item, index) => {
        const percentage = total > 0 ? ((item.value / total) * 100).toFixed(1) : 0;
        return `
            <tr>
                <td>${index + 1}</td>
                <td>
                    <span style="
                        display: inline-block;
                        width: 8px;
                        height: 8px;
                        border-radius: 2px;
                        background: ${chartColors[index % chartColors.length]};
                        margin-right: 8px;
                    "></span>
                    ${item.name}
                </td>
                <td>${formatNumber(item.value)}</td>
                <td>
                    <div style="
                        display: flex;
                        align-items: center;
                        gap: 8px;
                    ">
                        <div style="
                            flex: 1;
                            height: 6px;
                            background: rgba(255,255,255,0.1);
                            border-radius: 3px;
                            overflow: hidden;
                        ">
                            <div style="
                                width: ${percentage}%;
                                height: 100%;
                                background: ${chartColors[index % chartColors.length]};
                                border-radius: 3px;
                                transition: width 0.3s ease;
                            "></div>
                        </div>
                        <span style="font-size: 0.75rem; color: #a5b4fc;">${percentage}%</span>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

/**
 * Update region table
 */
function updateRegionTable(data) {
    // Filter out null/empty values
    const validData = data.filter(d => d.name && d.value > 0);
    
    if (!validData.length) {
        regionTableBody.innerHTML = `
            <tr>
                <td colspan="4" class="empty-state">Henüz bölge verisi yok - Simülasyonu başlatın</td>
            </tr>
        `;
        return;
    }

    const total = validData.reduce((sum, item) => sum + (item.value || 0), 0);

    regionTableBody.innerHTML = validData.map((item, index) => {
        const percentage = total > 0 ? ((item.value / total) * 100).toFixed(1) : 0;
        return `
            <tr>
                <td>${index + 1}</td>
                <td>
                    <span style="
                        display: inline-block;
                        width: 8px;
                        height: 8px;
                        border-radius: 2px;
                        background: ${chartColors[index % chartColors.length]};
                        margin-right: 8px;
                    "></span>
                    ${item.name}
                </td>
                <td>${formatNumber(item.value)}</td>
                <td>
                    <div style="
                        display: flex;
                        align-items: center;
                        gap: 8px;
                    ">
                        <div style="
                            flex: 1;
                            height: 6px;
                            background: rgba(255,255,255,0.1);
                            border-radius: 3px;
                            overflow: hidden;
                        ">
                            <div style="
                                width: ${percentage}%;
                                height: 100%;
                                background: ${chartColors[index % chartColors.length]};
                                border-radius: 3px;
                                transition: width 0.3s ease;
                            "></div>
                        </div>
                        <span style="font-size: 0.75rem; color: #a5b4fc;">${percentage}%</span>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

/**
 * Update connection status
 */
function updateConnectionStatus(connected) {
    if (connected) {
        connectionStatus.classList.remove('offline');
        connectionStatus.classList.add('online');
        connectionStatus.innerHTML = '<i class="fas fa-circle"></i> API Bağlı';
    } else {
        connectionStatus.classList.remove('online');
        connectionStatus.classList.add('offline');
        connectionStatus.innerHTML = '<i class="fas fa-circle"></i> API Bağlantısı Yok';
    }
}

/**
 * Format large numbers
 */
function formatNumber(num) {
    if (num >= 1000000) {
        return (num / 1000000).toFixed(1) + 'M';
    }
    if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'K';
    }
    return num.toString();
}

/**
 * Initialize Time Series Charts (Line Charts)
 */
function initializeTimeCharts() {
    // Hourly Chart - Line Chart
    const hourlyCtx = document.getElementById('hourlyChart').getContext('2d');
    hourlyChart = new Chart(hourlyCtx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'Arama Sayısı',
                data: [],
                borderColor: '#6366f1',
                backgroundColor: 'rgba(99, 102, 241, 0.1)',
                borderWidth: 3,
                fill: true,
                tension: 0.4,
                pointBackgroundColor: '#6366f1',
                pointBorderColor: '#fff',
                pointBorderWidth: 2,
                pointRadius: 4,
                pointHoverRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            animation: {
                duration: 500,
                easing: 'easeOutQuart'
            },
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    titleColor: '#fff',
                    bodyColor: '#fff',
                    padding: 12,
                    borderColor: 'rgba(255, 255, 255, 0.1)',
                    borderWidth: 1,
                    displayColors: false,
                    callbacks: {
                        label: function (context) {
                            return `${context.parsed.y} arama`;
                        }
                    }
                }
            },
            scales: {
                x: {
                    grid: {
                        color: 'rgba(255, 255, 255, 0.05)'
                    },
                    ticks: {
                        color: '#a5b4fc',
                        maxRotation: 45,
                        minRotation: 0,
                        font: {
                            size: 10
                        }
                    }
                },
                y: {
                    beginAtZero: true,
                    grace: '10%',
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)'
                    },
                    ticks: {
                        color: '#a5b4fc',
                        callback: function(value) {
                            if (Number.isInteger(value)) {
                                return value;
                            }
                        }
                    }
                }
            }
        }
    });

    // Daily Chart - Line Chart
    const dailyCtx = document.getElementById('dailyChart').getContext('2d');
    dailyChart = new Chart(dailyCtx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'Arama Sayısı',
                data: [],
                borderColor: '#ec4899',
                backgroundColor: 'rgba(236, 72, 153, 0.1)',
                borderWidth: 3,
                fill: true,
                tension: 0.4,
                pointBackgroundColor: '#ec4899',
                pointBorderColor: '#fff',
                pointBorderWidth: 2,
                pointRadius: 5,
                pointHoverRadius: 7
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            animation: {
                duration: 500,
                easing: 'easeOutQuart'
            },
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    titleColor: '#fff',
                    bodyColor: '#fff',
                    padding: 12,
                    borderColor: 'rgba(255, 255, 255, 0.1)',
                    borderWidth: 1,
                    displayColors: false,
                    callbacks: {
                        label: function (context) {
                            return `${context.parsed.y} arama`;
                        }
                    }
                }
            },
            scales: {
                x: {
                    grid: {
                        color: 'rgba(255, 255, 255, 0.05)'
                    },
                    ticks: {
                        color: '#a5b4fc',
                        font: {
                            size: 11
                        }
                    }
                },
                y: {
                    beginAtZero: true,
                    grace: '10%',
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)'
                    },
                    ticks: {
                        color: '#a5b4fc',
                        callback: function(value) {
                            if (Number.isInteger(value)) {
                                return value;
                            }
                        }
                    }
                }
            }
        }
    });
}

/**
 * Load hourly data from API
 */
async function loadHourlyData() {
    try {
        const response = await fetch(`${API_BASE}/stats/hourly`);
        const data = await response.json();

        if (data.status === 'success' && data.data) {
            updateHourlyChart(data.data);
        }
    } catch (error) {
        console.error('Hourly data fetch error:', error);
    }
}

/**
 * Load daily data from API
 */
async function loadDailyData() {
    try {
        const response = await fetch(`${API_BASE}/stats/daily`);
        const data = await response.json();

        if (data.status === 'success' && data.data) {
            updateDailyChart(data.data);
        }
    } catch (error) {
        console.error('Daily data fetch error:', error);
    }
}

/**
 * Update hourly line chart
 */
function updateHourlyChart(data) {
    if (!data || !data.length) {
        hourlyChart.data.labels = ['Veri Yok'];
        hourlyChart.data.datasets[0].data = [0];
    } else {
        hourlyChart.data.labels = data.map(d => d.hour);
        hourlyChart.data.datasets[0].data = data.map(d => d.count);
    }
    hourlyChart.update();
}

/**
 * Update daily line chart
 */
function updateDailyChart(data) {
    if (!data || !data.length) {
        dailyChart.data.labels = ['Veri Yok'];
        dailyChart.data.datasets[0].data = [0];
    } else {
        // Format date labels (e.g., "14 Ara" instead of "2025-12-14")
        dailyChart.data.labels = data.map(d => {
            const date = new Date(d.date);
            const day = date.getDate();
            const months = ['Oca', 'Şub', 'Mar', 'Nis', 'May', 'Haz', 'Tem', 'Ağu', 'Eyl', 'Eki', 'Kas', 'Ara'];
            return `${day} ${months[date.getMonth()]}`;
        });
        dailyChart.data.datasets[0].data = data.map(d => d.count);
    }
    dailyChart.update();
}
