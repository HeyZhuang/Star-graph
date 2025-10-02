package cn.itcast.star.graph.core.dto;

import lombok.Data;

@Data
public class PoseReqDto {
    private String imageBase64;  // 输入的姿势参考图片
    private String imageUrl;     // 或者使用图片URL
    private String poseType = "openpose";  // 姿势类型：openpose, depth, canny
    private int size;
    private int model;
    private int scale;
    private int step;
    private int cfg;
    private int sampler;
    private int seed;
    private String reverse;
    private String propmt;  // 描述目标图像的提示词
    private String clientId;
    private float controlStrength = 1.0f;  // 控制强度，0-1之间

    public String modelName(){
        switch (model){
            case 2:
                return "anythingelseV4_v45.safetensors";
            default:
                return "majicmixRealistic_v7.safetensors";
        }
    }

    public String samplerName(){
        switch (sampler){
            case 1:
                return "dpmpp_sde";
            case 2:
                return "dpmpp_2m";
            case 3:
                return "euler";
            case 4:
                return "dpmpp_3m_sde";
            default:
                return "euler";
        }
    }

    public String scheduler(){
        return "karras";
    }

    public int width() {
        if(scale==3){
            return 768;
        } else {
            return 512;
        }
    }

    public int height() {
        if(scale==2){
            return 768;
        } else {
            return 512;
        }
    }
}