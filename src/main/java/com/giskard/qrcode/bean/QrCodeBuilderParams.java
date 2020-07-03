package com.giskard.qrcode.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QrCodeBuilderParams {

    private String qrCodeText;

    private int qrCodeMargin;

    private int qrCodeSize;

    private String qrCodeRgbColorBackground;

    private String qrCodeRgbColorForeground;

    private boolean qrCodeLogoEnabled;

    private String qrCodeLogoPath;

    private int qrCodeLogoSize;

    private float qrCodeLogoTransparency;

}
