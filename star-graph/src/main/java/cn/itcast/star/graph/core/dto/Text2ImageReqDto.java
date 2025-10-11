package cn.itcast.star.graph.core.dto;

import lombok.Data;

@Data
public class Text2ImageReqDto {
    int size;
    int model;
    int scale;
    int step;
    int cfg;
    int sampler;
    int seed;
    String reverse;
    String propmt;
    String clientId;

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

    /**
     * 获取通义万相API支持的图片尺寸
     * 支持的尺寸: 1024*1024, 720*1280, 1280*720, 768*1152
     */
    public String tongyiSize() {
        // 根据scale映射到通义万相支持的尺寸
        switch (scale) {
            case 2: // 竖图 (原512x768)
                return "720*1280";
            case 3: // 横图 (原768x512)
                return "1280*720";
            case 1: // 方图 (原512x512)
            default:
                return "1024*1024";
        }
    }


}
