package cn.itcast.star.graph.core.dto;

import lombok.Data;

@Data
public class Image2VideoReqDto {
    private String imageBase64;      // Base64编码的输入图片
    private String imageUrl;         // 或者使用图片URL
    private String propmt;           // 视频描述（可选）
    private int duration = 4;        // 视频时长（秒）
    private int fps = 8;             // 帧率
    private int motionBucketId = 127; // 运动强度（0-255，值越大运动越明显）
    private float augmentationLevel = 0.0f; // 增强等级
    private float cfgScale = 2.5f;  // 配置缩放
    private int steps = 25;          // 生成步数
    private int seed = -1;           // 随机种子
    private String model = "svd";    // 使用的模型
    private String clientId;
    private boolean videoLoop = false; // 是否循环播放
    
    public String getModelName() {
        switch (model) {
            case "svd_image_decoder":
                return "svd_image_decoder.safetensors";
            case "i2vgen_xl":
                return "i2vgen_xl.safetensors";
            case "svd":
            default:
                return "svd_xt.safetensors";
        }
    }
}