package cn.itcast.star.graph.core.dto;

import lombok.Data;

@Data
public class UpscaleReqDto {
    private String imageBase64;  // Base64编码的图片
    private String imageUrl;     // 或者使用图片URL
    private float upscaleFactor = 2.0f;  // 放大倍数，默认2倍
    private String upscaleModel = "RealESRGAN_x4plus";  // 使用的放大模型
    private String clientId;
}