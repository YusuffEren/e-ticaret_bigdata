package com.bigdatacompany.eticaret.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * CORS yapılandırması - Frontend'in API'ye erişmesini sağlar
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Tüm origin'lere izin ver (geliştirme ortamı için)
        config.setAllowedOriginPatterns(Arrays.asList("*"));

        // İzin verilen HTTP metodları
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // İzin verilen header'lar
        config.setAllowedHeaders(Arrays.asList("*"));

        // Credentials'a izin ver
        config.setAllowCredentials(true);

        // Preflight cache süresi (1 saat)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
