package com.giskard.qrcode.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;


@Configuration
@EnableCaching
@Getter
public class CommonConfig {

    public static final String QR_CODE_CACHE = "qr-code-cache";
    public static final String LOGO_CACHE = "logo-cache";

    @Value("${qrcode.logo.enabled:true}")
    private boolean qrCodeLogoEnabled;

    @Value("${qrcode.logo.path:classpath:/logo.jpg}")
    private String qrCodeLogoPath;

    @Value("${qrcode.logo.size:50}")
    private int qrCodeLogoSize;

    @Value("${qrcode.logo.transparency:1.0f}")
    private float qrCodeLogoTransparency;

    @Value("${qrcode.margin:1}")
    private int qrCodeMargin;

    @Value("${qrcode.size:300}")
    private int qrCodeSize;

    @Value("${qrcode.rgbColorBackground:#FFFFFF}")
    private String qrCodeRgbColorBackground;

    @Value("${qrcode.rgbColorForeground:#000000}")
    private String qrCodeRgbColorForeground;

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .initialCapacity(1000)
                .maximumSize(5000)
                .expireAfterAccess(30, TimeUnit.DAYS);
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(QR_CODE_CACHE, LOGO_CACHE);
        caffeineCacheManager.setCaffeine(caffeine);
        return caffeineCacheManager;
    }
}
