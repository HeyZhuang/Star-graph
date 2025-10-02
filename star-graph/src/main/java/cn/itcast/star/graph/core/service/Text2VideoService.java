package cn.itcast.star.graph.core.service;

import cn.itcast.star.graph.core.dto.Text2VideoReqDto;
import cn.itcast.star.graph.core.dto.VideoGenerationResponseDto;

public interface Text2VideoService {
    VideoGenerationResponseDto textToVideo(Text2VideoReqDto text2VideoReqDto) throws Exception;
}