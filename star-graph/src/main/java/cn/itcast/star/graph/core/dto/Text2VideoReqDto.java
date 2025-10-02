package cn.itcast.star.graph.core.dto;

import lombok.Data;

@Data
public class Text2VideoReqDto {
    private String propmt;           // 视频描述提示词
    private String negativePrompt;   // 负面提示词
    private int duration = 4;        // 视频时长（秒）
    private int fps = 8;             // 帧率
    private int width = 512;         // 视频宽度
    private int height = 512;        // 视频高度
    private int motionBucketId = 127; // 运动强度参数
    private float cfgScale = 7.5f;  // 配置缩放
    private int steps = 25;          // 生成步数
    private int seed = -1;           // 随机种子
    private String model = "svd";    // 使用的模型（svd, zeroscope等）
    private String clientId;
    
    public String getModelName() {
        switch (model) {
            case "zeroscope":
                return "zeroscope_v2_576w.safetensors";
            case "animatediff":
                return "animatediff_motion_v15.ckpt";
            case "svd":
            default:
                return "svd_xt.safetensors";
        }
    }
}