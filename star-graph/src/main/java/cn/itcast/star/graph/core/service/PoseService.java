package cn.itcast.star.graph.core.service;

import cn.itcast.star.graph.core.dto.PoseReqDto;
import cn.itcast.star.graph.core.dto.Text2ImageResponeDto;

public interface PoseService {
    Text2ImageResponeDto generateFromPose(PoseReqDto poseReqDto) throws Exception;
}