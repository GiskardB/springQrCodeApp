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

    @Value("${qrcode.logo.enabled}")
    private boolean qrCodeLogoEnabled;

    @Value("${qrcode.logo.path}")
    private String qrCodeLogoPath;

    @Value("${qrcode.logo.size}")
    private int qrCodeLogoSize;

    @Value("${qrcode.logo.transparency}")
    private float qrCodeLogoTransparency;

    @Value("${qrcode.margin}")
    private int qrCodeMargin;

    @Value("${qrcode.size}")
    private int qrCodeSize;

    @Value("${qrcode.rgbColorBackground}")
    private String qrCodeRgbColorBackground;

    @Value("${qrcode.rgbColorForeground}")
    private String qrCodeRgbColorForeground;

    @Bean
    public Caffeine caffeineConfig() {
        return Caffeine.newBuilder()
                .initialCapacity(1000)
                .maximumSize(5000)
                .expireAfterAccess(30, TimeUnit.DAYS);
    }

    @Bean
    public CacheManager cacheManager(Caffeine caffeine) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(QR_CODE_CACHE, LOGO_CACHE);
        caffeineCacheManager.setCaffeine(caffeine);
        return caffeineCacheManager;
    }
}
