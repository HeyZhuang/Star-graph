package cn.itcast.star.graph.core.controller;

import cn.itcast.star.graph.core.dto.PoseReqDto;
import cn.itcast.star.graph.core.dto.Text2ImageResponeDto;
import cn.itcast.star.graph.core.dto.common.Result;
import cn.itcast.star.graph.core.service.PoseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 姿势视图接口
 * </p>
 *
 * @author luoxu
 * @since 2024-10-15
 */
@RestController
@RequestMapping("/api/authed/1.0/pose")
public class PoseController {

    @Autowired
    private PoseService poseService;

    @PostMapping("/generate")
    public Result<Text2ImageResponeDto> generateFromPose(@RequestBody PoseReqDto poseReqDto) throws Exception {
        Text2ImageResponeDto responeDto = poseService.generateFromPose(poseReqDto);
        return Result.ok(responeDto);
    }
}