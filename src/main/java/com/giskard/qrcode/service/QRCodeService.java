package com.giskard.qrcode.service;

import com.giskard.qrcode.bean.QrCodeBuilderParams;
import com.giskard.qrcode.config.CommonConfig;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
@CacheConfig(cacheNames = CommonConfig.QR_CODE_CACHE)
public class QRCodeService implements InitializingBean {

    @Autowired
    CommonConfig commonConfig;

    @Autowired
    CacheManager cacheManager;

    public Mono<byte[]> generateQRCode(String text, int width, int height) {

        Assert.hasText(text, "text must not be empty");

        QrCodeBuilderParams params = QrCodeBuilderParams.builder()
                .qrCodeText(text)
                .qrCodeMargin(this.commonConfig.getQrCodeMargin())
                .qrCodeSize(this.commonConfig.getQrCodeSize())
                .qrCodeRgbColorBackground(this.commonConfig.getQrCodeRgbColorBackground())
                .qrCodeRgbColorForeground(this.commonConfig.getQrCodeRgbColorForeground())
                .qrCodeLogoEnabled(this.commonConfig.isQrCodeLogoEnabled())
                .qrCodeLogoPath(this.commonConfig.getQrCodeLogoPath())
                .qrCodeLogoSize(this.commonConfig.getQrCodeLogoSize())
                .qrCodeLogoTransparency(this.commonConfig.getQrCodeLogoTransparency()).build();

        String key = buildKey(text, width, height);

        return Mono.create(sink -> {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Cache.ValueWrapper cacheByte = this.cacheManager.getCache(CommonConfig.QR_CODE_CACHE).get(key);
                if (cacheByte != null) {
                    log.debug("GetQRCode from Cache");
                    baos.write((byte[]) cacheByte.get());
                } else {
                    log.info("Generate QrCode");
                    baos = this.buildQrCode(params);
                    this.cacheManager.getCache(CommonConfig.QR_CODE_CACHE).put(key, baos.toByteArray());
                }
                sink.success(baos.toByteArray());
            } catch (Throwable ex) {
                log.error("Error", ex);
                sink.error(ex);
            }
        });
    }

    private String buildKey(String text, int width, int height) {
        return text + "" + width + "" + height;
    }


    private ByteArrayOutputStream buildQrCode(QrCodeBuilderParams params) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            // Create a qr code
            BitMatrix bitMatrix = new QRCodeWriter().encode(params.getQrCodeText(), BarcodeFormat.QR_CODE, params.getQrCodeSize(), params.getQrCodeSize(), buildHints(params));
            MatrixToImageConfig config = new MatrixToImageConfig(
                    Color.decode(params.getQrCodeRgbColorForeground()).getRGB(),
                    Color.decode(params.getQrCodeRgbColorBackground()).getRGB());

            // Load QR image
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix, config);
            BufferedImage finalQrCode = qrImage;
            if (params.isQrCodeLogoEnabled()) {
                finalQrCode = addLogo(params, qrImage);
                ;
            }

            // Write final image as PNG to OutputStream
            ImageIO.write(finalQrCode, "png", baos);

        } catch (WriterException | IOException e) {
            log.error("Error", e);
        }
        return baos;
    }

    private Map<EncodeHintType, ?> buildHints(QrCodeBuilderParams params) {
        Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, params.getQrCodeMargin());
        return hints;
    }

    private BufferedImage addLogo(QrCodeBuilderParams params, BufferedImage qrImage) throws IOException {
        BufferedImage logoImage = readImage(params.getQrCodeLogoPath());

        // Initialize combined image
        BufferedImage combined = new BufferedImage(params.getQrCodeSize(), params.getQrCodeSize(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) combined.getGraphics();

        // Write QR code to new image at position 0/0
        g.drawImage(qrImage, 0, 0, null);

        Image scaledInstance = logoImage.getScaledInstance(params.getQrCodeLogoSize(), params.getQrCodeLogoSize(), BufferedImage.SCALE_SMOOTH);
        // Calculate the delta height and width between QR code and logo
        int deltaHeight = qrImage.getHeight() - scaledInstance.getHeight(null);
        int deltaWidth = qrImage.getWidth() - scaledInstance.getWidth(null);
        g.drawImage(scaledInstance,
                (int) Math.round(deltaWidth / 2), (int) Math.round(deltaHeight / 2),
                new Color(0, 0, 0), null);
        return combined;
    }

    @Cacheable
    BufferedImage readImage(String path) throws IOException {

        Cache.ValueWrapper cacheLogo = this.cacheManager.getCache(CommonConfig.LOGO_CACHE).get(path);

        if (cacheLogo != null) {
            log.debug("Get Logo from Cache");
            return (BufferedImage) cacheLogo.get();
        } else {
            log.info("Read Logo Image");
            BufferedImage bufferedImage;
            if (path.startsWith("http://")
                    || path.startsWith("https://")) {
                bufferedImage = ImageIO.read(new URL(path).openStream());
            } else if (path.startsWith("classpath:")) {
                bufferedImage = ImageIO.read(ClassLoader.getSystemResource(path.replace("classpath:", "")));
            } else {
                bufferedImage = ImageIO.read(new File(path));
            }

            if (bufferedImage != null) {
                this.cacheManager.getCache(CommonConfig.LOGO_CACHE).put(path, bufferedImage);
            }
            return bufferedImage;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        //TODO: controlli sui parametri


    }
}