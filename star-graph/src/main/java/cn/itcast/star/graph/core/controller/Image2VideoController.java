package cn.itcast.star.graph.core.controller;

import cn.itcast.star.graph.core.dto.Image2VideoReqDto;
import cn.itcast.star.graph.core.dto.VideoGenerationResponseDto;
import cn.itcast.star.graph.core.dto.common.Result;
import cn.itcast.star.graph.core.service.Image2VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 图生视频接口
 * </p>
 *
 * @author luoxu
 * @since 2024-10-15
 */
@RestController
@RequestMapping("/api/authed/1.0/i2v")
public class Image2VideoController {

    @Autowired
    private Image2VideoService image2VideoService;

    @PostMapping("/generate")
    public Result<VideoGenerationResponseDto> generateVideo(@RequestBody Image2VideoReqDto image2VideoReqDto) throws Exception {
        VideoGenerationResponseDto responseDto = image2VideoService.imageToVideo(image2VideoReqDto);
        return Result.ok(responseDto);
    }
}