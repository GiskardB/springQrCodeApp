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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@Service
@Log4j2
public class QRCodeService {

    @Autowired
    CommonConfig commonConfig;

    @Autowired
    CacheManager cacheManager;

    public Mono<byte[]> generateQRCode(String text) {
        Assert.hasText(text, "text must not be empty");

        return generateQRCode(QrCodeBuilderParams.builder()
                .qrCodeText(text)
                .qrCodeMargin(this.commonConfig.getQrCodeMargin())
                .qrCodeSize(this.commonConfig.getQrCodeSize())
                .qrCodeRgbColorBackground(this.commonConfig.getQrCodeRgbColorBackground())
                .qrCodeRgbColorForeground(this.commonConfig.getQrCodeRgbColorForeground())
                .qrCodeLogoEnabled(this.commonConfig.isQrCodeLogoEnabled())
                .qrCodeLogoPath(this.commonConfig.getQrCodeLogoPath())
                .qrCodeLogoSize(this.commonConfig.getQrCodeLogoSize())
                .qrCodeLogoTransparency(this.commonConfig.getQrCodeLogoTransparency()).build());
    }

    public Mono<byte[]> generateQRCode(QrCodeBuilderParams params) {
        Assert.notNull(params, "text must not be empty");

        return Mono.create(sink -> {
            try {
                //Build Cache Key
                String key = buildCacheKey(params);

                byte[] qrCodeBytes;

                Cache.ValueWrapper cacheByte = this.cacheManager.getCache(CommonConfig.QR_CODE_CACHE).get(key);
                if (cacheByte != null) {
                    log.debug("GetQRCode from Cache");
                    qrCodeBytes = (byte[]) cacheByte.get();
                } else {
                    log.info("Generate QrCode");
                    qrCodeBytes = this.buildQrCode(params);
                    this.cacheManager.getCache(CommonConfig.QR_CODE_CACHE).put(key, qrCodeBytes);
                }
                sink.success(qrCodeBytes);
            } catch (Exception ex) {
                log.error("Error", ex);
                sink.error(ex);
            }
        });
    }

    private String buildCacheKey(QrCodeBuilderParams params) {
        return params.getQrCodeText() + "" + params.getQrCodeLogoSize();
    }

    private byte[] buildQrCode(QrCodeBuilderParams params) throws IOException, WriterException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            // Create a qr code
            BitMatrix bitMatrix = new QRCodeWriter().encode(params.getQrCodeText(), BarcodeFormat.QR_CODE, params.getQrCodeSize(), params.getQrCodeSize(), buildHints(params));
            MatrixToImageConfig config = new MatrixToImageConfig(
                    Color.decode(params.getQrCodeRgbColorForeground()).getRGB(),
                    Color.decode(params.getQrCodeRgbColorBackground()).getRGB());

            // Load QR image
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix, config);
            BufferedImage finalQrCode = params.isQrCodeLogoEnabled() ? addLogo(params, qrImage) : qrImage;

            // Write final image as PNG to OutputStream
            ImageIO.write(finalQrCode, "png", baos);

        } catch (WriterException | IOException e) {
            log.error("Error", e);
            throw e;
        }
        return baos.toByteArray();
    }

    private Map<EncodeHintType, ?> buildHints(QrCodeBuilderParams params) {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, params.getQrCodeMargin());
        return hints;
    }

    private BufferedImage addLogo(QrCodeBuilderParams params, BufferedImage qrImage) throws IOException {
        Optional<BufferedImage> bufferedImage = readImage(params.getQrCodeLogoPath());
        BufferedImage logoImage = bufferedImage.get();
        // Initialize combined image
        BufferedImage combined = new BufferedImage(params.getQrCodeSize(), params.getQrCodeSize(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) combined.getGraphics();

        // Write QR code to new image at position 0/0
        g.drawImage(qrImage, 0, 0, null);

        Image scaledInstance = logoImage.getScaledInstance(params.getQrCodeLogoSize(), params.getQrCodeLogoSize(), Image.SCALE_SMOOTH);
        // Calculate the delta height and width between QR code and logo
        int deltaHeight = qrImage.getHeight() - scaledInstance.getHeight(null);
        int deltaWidth = qrImage.getWidth() - scaledInstance.getWidth(null);
        g.drawImage(scaledInstance,
                Math.round(deltaWidth / 2f), Math.round(deltaHeight / 2f),
                new Color(0, 0, 0), null);
        return combined;
    }

    private Optional<BufferedImage> readImage(String path) throws IOException {

        Cache.ValueWrapper cacheLogo = this.cacheManager.getCache(CommonConfig.LOGO_CACHE).get(path);

        if (cacheLogo != null) {
            log.debug("Get Logo from Cache");
            return Optional.of((BufferedImage) cacheLogo.get());
        } else {
            log.info("Read Logo Image");
            BufferedImage bufferedImage;
            if (path.startsWith("http://")
                    || path.startsWith("https://")) {
                bufferedImage = ImageIO.read(new URL(path).openStream());
            } else if (path.startsWith("classpath:")) {
                bufferedImage = ImageIO.read(getClass().getResource(path.replace("classpath:", "")));
            } else {
                bufferedImage = ImageIO.read(new File(path));
            }

            if (bufferedImage != null) {
                this.cacheManager.getCache(CommonConfig.LOGO_CACHE).put(path, bufferedImage);
            }
            return Optional.of(bufferedImage);
        }
    }
}