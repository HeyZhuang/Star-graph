package cn.itcast.star.graph.core.controller;

import cn.itcast.star.graph.core.dto.UpscaleReqDto;
import cn.itcast.star.graph.core.dto.Text2ImageResponeDto;
import cn.itcast.star.graph.core.dto.common.Result;
import cn.itcast.star.graph.core.service.UpscaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 画质提升接口
 * </p>
 *
 * @author luoxu
 * @since 2024-10-15
 */
@RestController
@RequestMapping("/api/authed/1.0/upscale")
public class UpscaleController {

    @Autowired
    private UpscaleService upscaleService;

    @PostMapping("/enhance")
    public Result<Text2ImageResponeDto> enhanceImage(@RequestBody UpscaleReqDto upscaleReqDto) throws Exception {
        Text2ImageResponeDto responeDto = upscaleService.upscaleImage(upscaleReqDto);
        return Result.ok(responeDto);
    }
}