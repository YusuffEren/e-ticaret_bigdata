/**
 * E-Ticaret Analytics - Main Application JS
 * Arama sayfası işlevselliği
 */

// API Base URL
const API_BASE = 'http://localhost:8081/api';

// State
let recentSearches = [];
let isSimulationRunning = false;

// DOM Elements
const searchInput = document.getElementById('search-input');
const searchBtn = document.getElementById('search-btn');
const regionSelect = document.getElementById('region-select');
const searchResult = document.getElementById('search-result');
const resultDetails = document.getElementById('result-details');
const startSimulationBtn = document.getElementById('start-simulation');
const stopSimulationBtn = document.getElementById('stop-simulation');
const intervalInput = document.getElementById('interval-input');
const messageCountEl = document.getElementById('message-count');
const simulationStatusEl = document.getElementById('simulation-status');
const recentSearchesEl = document.getElementById('recent-searches');
const clearHistoryBtn = document.getElementById('clear-history');
const connectionStatus = document.getElementById('connection-status');

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    initializeApp();
    setupEventListeners();
    loadRecentSearches();
    checkApiStatus();
});

/**
 * Initialize the application
 */
function initializeApp() {
    // Check API status periodically
    setInterval(checkApiStatus, 5000);
}

/**
 * Setup event listeners
 */
function setupEventListeners() {
    // Search button click
    searchBtn.addEventListener('click', handleSearch);

    // Enter key in search input
    searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            handleSearch();
        }
    });

    // Quick tags
    document.querySelectorAll('.quick-tag').forEach(tag => {
        tag.addEventListener('click', () => {
            const searchTerm = tag.dataset.search;
            searchInput.value = searchTerm;
            handleSearch();
        });
    });

    // Simulation controls
    startSimulationBtn.addEventListener('click', startSimulation);
    stopSimulationBtn.addEventListener('click', stopSimulation);

    // Clear history
    clearHistoryBtn.addEventListener('click', clearHistory);
}

/**
 * Handle search submission
 */
async function handleSearch() {
    const searchTerm = searchInput.value.trim();

    if (!searchTerm) {
        showNotification('Lütfen bir arama terimi girin', 'warning');
        return;
    }

    // Get region
    let region = regionSelect.value;
    if (region === 'random') {
        const regions = ['İstanbul', 'Ankara', 'İzmir', 'Bursa', 'Antalya', 'Adana', 'Gaziantep', 'Mersin', 'Samsun', 'Ordu'];
        region = regions[Math.floor(Math.random() * regions.length)];
    }

    try {
        // For now, we'll use the simulation endpoint since there's no direct search endpoint
        // In a real app, you'd have a dedicated endpoint for single searches

        // Add to recent searches
        addToRecentSearches(searchTerm, region);

        // Show success message
        showSearchResult(searchTerm, region);

        // Clear input
        searchInput.value = '';

    } catch (error) {
        console.error('Search error:', error);
        showNotification('Arama gönderilirken hata oluştu', 'error');
    }
}

/**
 * Show search result notification
 */
function showSearchResult(search, region) {
    searchResult.classList.remove('hidden');
    resultDetails.textContent = `"${search}" araması ${region} bölgesinden gönderildi`;

    // Hide after 3 seconds
    setTimeout(() => {
        searchResult.classList.add('hidden');
    }, 3000);
}

/**
 * Add to recent searches
 */
function addToRecentSearches(search, region) {
    const timestamp = new Date().toLocaleTimeString('tr-TR');

    recentSearches.unshift({
        search,
        region,
        timestamp
    });

    // Keep only last 20
    if (recentSearches.length > 20) {
        recentSearches.pop();
    }

    // Save to localStorage
    localStorage.setItem('recentSearches', JSON.stringify(recentSearches));

    // Update UI
    renderRecentSearches();
}

/**
 * Load recent searches from localStorage
 */
function loadRecentSearches() {
    const saved = localStorage.getItem('recentSearches');
    if (saved) {
        recentSearches = JSON.parse(saved);
        renderRecentSearches();
    }
}

/**
 * Render recent searches list
 */
function renderRecentSearches() {
    if (recentSearches.length === 0) {
        recentSearchesEl.innerHTML = '<p class="empty-state">Henüz arama yapılmadı</p>';
        return;
    }

    recentSearchesEl.innerHTML = recentSearches.map(item => `
        <div class="recent-item">
            <div class="item-info">
                <span class="item-search">🔍 ${item.search}</span>
                <span class="item-region">📍 ${item.region}</span>
            </div>
            <span class="item-time">${item.timestamp}</span>
        </div>
    `).join('');
}

/**
 * Clear search history
 */
function clearHistory() {
    recentSearches = [];
    localStorage.removeItem('recentSearches');
    renderRecentSearches();
    showNotification('Arama geçmişi temizlendi', 'success');
}

/**
 * Start simulation
 */
async function startSimulation() {
    const interval = intervalInput.value || 1000;

    try {
        const response = await fetch(`${API_BASE}/search?intervalMs=${interval}`);
        const data = await response.json();

        if (data.status === 'success') {
            isSimulationRunning = true;
            updateSimulationUI(true);
            showNotification('Simülasyon başlatıldı!', 'success');

            // Start polling for status
            pollSimulationStatus();
        } else {
            showNotification(data.message || 'Simülasyon başlatılamadı', 'error');
        }
    } catch (error) {
        console.error('Start simulation error:', error);
        showNotification('API bağlantısı sağlanamadı', 'error');
    }
}

/**
 * Stop simulation
 */
async function stopSimulation() {
    try {
        const response = await fetch(`${API_BASE}/stop`);
        const data = await response.json();

        if (data.status === 'success') {
            isSimulationRunning = false;
            updateSimulationUI(false);
            messageCountEl.textContent = data.totalMessages || 0;
            showNotification('Simülasyon durduruldu', 'success');
        } else {
            showNotification(data.message || 'Simülasyon durdurulamadı', 'error');
        }
    } catch (error) {
        console.error('Stop simulation error:', error);
        showNotification('API bağlantısı sağlanamadı', 'error');
    }
}

/**
 * Poll simulation status
 */
async function pollSimulationStatus() {
    if (!isSimulationRunning) return;

    try {
        const response = await fetch(`${API_BASE}/status`);
        const data = await response.json();

        messageCountEl.textContent = data.messageCount || 0;

        if (!data.isRunning) {
            isSimulationRunning = false;
            updateSimulationUI(false);
        } else {
            // Continue polling
            setTimeout(pollSimulationStatus, 1000);
        }
    } catch (error) {
        console.error('Status poll error:', error);
    }
}

/**
 * Update simulation UI
 */
function updateSimulationUI(running) {
    startSimulationBtn.disabled = running;
    stopSimulationBtn.disabled = !running;
    simulationStatusEl.textContent = running ? 'Çalışıyor' : 'Durdu';
    simulationStatusEl.style.color = running ? '#10b981' : '#ef4444';
}

/**
 * Check API status
 */
async function checkApiStatus() {
    try {
        const response = await fetch(`${API_BASE}/status`);
        const data = await response.json();

        // Update connection status
        connectionStatus.classList.remove('offline');
        connectionStatus.classList.add('online');
        connectionStatus.innerHTML = '<i class="fas fa-circle"></i> API Bağlı';

        // Update simulation state
        if (data.isRunning !== undefined) {
            isSimulationRunning = data.isRunning;
            updateSimulationUI(data.isRunning);
            messageCountEl.textContent = data.messageCount || 0;
        }

    } catch (error) {
        connectionStatus.classList.remove('online');
        connectionStatus.classList.add('offline');
        connectionStatus.innerHTML = '<i class="fas fa-circle"></i> API Bağlantısı Yok';
    }
}

/**
 * Show notification
 */
function showNotification(message, type = 'info') {
    // Simple console notification for now
    // In a real app, you'd use a toast notification library
    console.log(`[${type.toUpperCase()}] ${message}`);

    // Also update the search result area temporarily
    if (type === 'error') {
        searchResult.style.background = 'rgba(239, 68, 68, 0.1)';
        searchResult.style.borderColor = '#ef4444';
        searchResult.querySelector('.result-icon i').className = 'fas fa-exclamation-circle';
        searchResult.querySelector('.result-icon').style.color = '#ef4444';
    } else if (type === 'warning') {
        searchResult.style.background = 'rgba(245, 158, 11, 0.1)';
        searchResult.style.borderColor = '#f59e0b';
        searchResult.querySelector('.result-icon i').className = 'fas fa-exclamation-triangle';
        searchResult.querySelector('.result-icon').style.color = '#f59e0b';
    } else {
        searchResult.style.background = 'rgba(16, 185, 129, 0.1)';
        searchResult.style.borderColor = '#10b981';
        searchResult.querySelector('.result-icon i').className = 'fas fa-check-circle';
        searchResult.querySelector('.result-icon').style.color = '#10b981';
    }

    resultDetails.textContent = message;
    searchResult.classList.remove('hidden');

    setTimeout(() => {
        searchResult.classList.add('hidden');
    }, 3000);
}
