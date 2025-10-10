package cn.itcast.star.graph.core.service;

import cn.itcast.star.graph.core.dto.Text2ImageReqDto;
import cn.itcast.star.graph.core.dto.Text2ImageResponeDto;

/**
 * 通义万相图像生成服务接口
 */
public interface TongyiImageService {

    /**
     * 文生图
     * @param reqDto 请求参数
     * @return 响应结果
     */
    Text2ImageResponeDto textToImage(Text2ImageReqDto reqDto) throws Exception;
}
