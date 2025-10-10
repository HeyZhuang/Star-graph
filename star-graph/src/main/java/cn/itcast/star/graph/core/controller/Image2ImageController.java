package cn.itcast.star.graph.core.controller;

import cn.itcast.star.graph.core.dto.Image2ImageReqDto;
import cn.itcast.star.graph.core.dto.Text2ImageResponeDto;
import cn.itcast.star.graph.core.dto.common.Result;
import cn.itcast.star.graph.core.service.Image2ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 图生图接口
 * </p>
 *
 * @author luoxu
 * @since 2024-10-15
 */
@RestController
@RequestMapping("/api/authed/1.0/i2i")
public class Image2ImageController {

    @Autowired
    private Image2ImageService image2ImageService;

    @PostMapping("/generate")
    public Result<Text2ImageResponeDto> generateImage(@RequestBody Image2ImageReqDto image2ImageReqDto) throws Exception {
        Text2ImageResponeDto responeDto = image2ImageService.imageToImage(image2ImageReqDto);
        return Result.ok(responeDto);
    }
}