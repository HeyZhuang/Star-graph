package cn.itcast.star.graph.core.dto;

import lombok.Data;

@Data
public class VideoGenerationResponseDto {
    private String code;
    private String msg;
    private String pid;        // 任务ID
    private Integer queueIndex; // 队列位置
    private String videoUrl;   // 生成的视频URL
    private String previewUrl; // 预览图URL
    private Integer duration;  // 视频时长
    private Integer fps;       // 帧率
}