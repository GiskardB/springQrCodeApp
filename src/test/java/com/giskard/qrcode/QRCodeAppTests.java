package com.giskard.qrcode;

import com.giskard.qrcode.bean.QrCodeBuilderParams;
import com.giskard.qrcode.config.CommonConfig;
import com.giskard.qrcode.service.QRCodeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureWebTestClient
public class QRCodeAppTests {

	@Autowired
	private QRCodeService imageService;

	@Autowired
	CommonConfig commonConfig;

	@Test
	public void testImageServiceQrCodeGenerationSuccess() throws Exception {
		byte[] imageBlob = imageService.generateQRCode("This is a test").block();
		assertNotNull(imageBlob);

		//Cover Cache
		byte[] imageBlobFromCache = imageService.generateQRCode("This is a test").block();
		assertNotNull(imageBlobFromCache);

		assertTrue(Arrays.equals(imageBlob, imageBlobFromCache));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testImageServiceQrCodeGenerationErrorNullText() throws Exception {
		imageService.generateQRCode((String) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testImageServiceQrCodeGenerationErrorEmptyText() throws Exception {
		imageService.generateQRCode("");
	}

	@Test(expected = Exception.class)
	public void testImageServiceQrCodeGenerationErrorInvalidParams() throws Exception {
		imageService.generateQRCode(QrCodeBuilderParams.builder()
				.qrCodeText("this is test exception")
				.qrCodeMargin(this.commonConfig.getQrCodeMargin())
				.qrCodeSize(this.commonConfig.getQrCodeSize())
				.qrCodeRgbColorBackground(this.commonConfig.getQrCodeRgbColorBackground())
				.qrCodeRgbColorForeground(this.commonConfig.getQrCodeRgbColorForeground())
				.qrCodeLogoEnabled(this.commonConfig.isQrCodeLogoEnabled())
				.qrCodeLogoPath("path exception")
				.qrCodeLogoSize(this.commonConfig.getQrCodeLogoSize())
				.qrCodeLogoTransparency(this.commonConfig.getQrCodeLogoTransparency()).build()).block();
	}
}
