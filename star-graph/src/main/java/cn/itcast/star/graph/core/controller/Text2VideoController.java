package cn.itcast.star.graph.core.controller;

import cn.itcast.star.graph.core.dto.Text2VideoReqDto;
import cn.itcast.star.graph.core.dto.VideoGenerationResponseDto;
import cn.itcast.star.graph.core.dto.common.Result;
import cn.itcast.star.graph.core.service.Text2VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 文生视频接口
 * </p>
 *
 * @author luoxu
 * @since 2024-10-15
 */
@RestController
@RequestMapping("/api/authed/1.0/t2v")
public class Text2VideoController {

    @Autowired
    private Text2VideoService text2VideoService;

    @PostMapping("/generate")
    public Result<VideoGenerationResponseDto> generateVideo(@RequestBody Text2VideoReqDto text2VideoReqDto) throws Exception {
        VideoGenerationResponseDto responseDto = text2VideoService.textToVideo(text2VideoReqDto);
        return Result.ok(responseDto);
    }
}