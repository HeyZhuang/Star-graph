package cn.itcast.star.graph.core.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class Image2ImageReqDto {
    private String imageBase64;  // Base64编码的图片
    private String imageUrl;     // 或者使用图片URL
    private float strength = 0.75f;  // 图生图强度，0-1之间，越高越接近原图
    private int size;
    private int model;
    private int scale;
    private int step;
    private int cfg;
    private int sampler;
    private int seed;
    private String reverse;
    private String propmt;
    private String clientId;

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