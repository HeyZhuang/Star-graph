package cn.itcast.star.graph.core.service;

import cn.itcast.star.graph.core.dto.UpscaleReqDto;
import cn.itcast.star.graph.core.dto.Text2ImageResponeDto;

public interface UpscaleService {
    Text2ImageResponeDto upscaleImage(UpscaleReqDto upscaleReqDto) throws Exception;
}