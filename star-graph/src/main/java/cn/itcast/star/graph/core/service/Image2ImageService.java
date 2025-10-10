package cn.itcast.star.graph.core.service;

import cn.itcast.star.graph.core.dto.Image2ImageReqDto;
import cn.itcast.star.graph.core.dto.Text2ImageResponeDto;

public interface Image2ImageService {
    Text2ImageResponeDto imageToImage(Image2ImageReqDto image2ImageReqDto) throws Exception;
}