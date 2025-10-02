package cn.itcast.star.graph.core.service;

import cn.itcast.star.graph.core.dto.Text2ImageReqDto;
import cn.itcast.star.graph.core.dto.Text2ImageResponeDto;

public interface Text2ImageService {
    Text2ImageResponeDto textToImage(Text2ImageReqDto text2ImageReqDto) throws Exception;
}
