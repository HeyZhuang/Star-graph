package cn.itcast.star.graph.core.service;

import cn.itcast.star.graph.core.dto.Image2VideoReqDto;
import cn.itcast.star.graph.core.dto.VideoGenerationResponseDto;

public interface Image2VideoService {
    VideoGenerationResponseDto imageToVideo(Image2VideoReqDto image2VideoReqDto) throws Exception;
}