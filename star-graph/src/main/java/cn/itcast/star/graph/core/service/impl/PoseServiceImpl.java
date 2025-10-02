package cn.itcast.star.graph.core.service.impl;

import cn.itcast.star.graph.comfyui.client.pojo.ComfyuiRequestDto;
import cn.itcast.star.graph.comfyui.client.pojo.ComfyuiTask;
import cn.itcast.star.graph.core.common.Constants;
import cn.itcast.star.graph.core.dto.PoseReqDto;
import cn.itcast.star.graph.core.dto.Text2ImageResponeDto;
import cn.itcast.star.graph.core.exception.CustomException;
import cn.itcast.star.graph.core.service.*;
import cn.itcast.star.graph.core.utils.UserUtils;
import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PoseServiceImpl implements PoseService {

    @Autowired
    OllamaService ollamaService;
    @Autowired
    FreemarkerService freemarkerService;
    @Autowired
    RedisService redisService;
    @Autowired
    UserFundRecordService userFundRecordService;

    @Override
    public Text2ImageResponeDto generateFromPose(PoseReqDto poseReqDto) throws Exception {
        // 检查用户余额
        Long userId = UserUtils.getUserId();
        int balance = userFundRecordService.checkBalance(userId);
        if(balance < 8){
            throw new CustomException("余额不足，请充值");
        }

        // 构建ComfyUI任务
        ComfyuiTask comfyuiTask = getComfyuiTask(poseReqDto);
        
        // 添加到任务队列
        redisService.addTask(comfyuiTask);
        
        // 扣费（姿势视图消耗8个积分）
        userFundRecordService.deductBalance(userId, 8);
        
        Text2ImageResponeDto responeDto = new Text2ImageResponeDto();
        responeDto.setCode("200");
        responeDto.setMsg("姿势生成任务已提交");
        return responeDto;
    }

    private ComfyuiTask getComfyuiTask(PoseReqDto poseReqDto) throws Exception {
        // 创建模型参数
        Map<String, Object> modelParams = new HashMap<>();
        modelParams.put("model_name", poseReqDto.modelName());
        modelParams.put("scheduler", poseReqDto.scheduler());
        modelParams.put("width", poseReqDto.width());
        modelParams.put("height", poseReqDto.height());
        modelParams.put("sampler_name", poseReqDto.samplerName());
        modelParams.put("steps", poseReqDto.getStep());
        modelParams.put("cfg", poseReqDto.getCfg());
        modelParams.put("seed", poseReqDto.getSeed());
        modelParams.put("pose_type", poseReqDto.getPoseType());
        modelParams.put("control_strength", poseReqDto.getControlStrength());
        
        // 处理输入图片
        String inputImage = poseReqDto.getImageBase64();
        if(inputImage == null && poseReqDto.getImageUrl() != null){
            // TODO: 从URL下载图片并转为base64
            inputImage = downloadImageAsBase64(poseReqDto.getImageUrl());
        }
        modelParams.put("input_image", inputImage);
        
        // 转换提示词
        String translatedPrompt = ollamaService.translate(poseReqDto.getPropmt());
        modelParams.put("positive_prompt", "(8k, best quality, masterpiece),(high detailed skin)," + translatedPrompt);
        
        String translatedNegative = ollamaService.translate(poseReqDto.getReverse());
        modelParams.put("negative_prompt", translatedNegative + ",bad face,naked,bad finger,bad arm,bad leg,bad eye");
        
        // 生成ComfyUI工作流
        String workflow = freemarkerService.renderPose(modelParams);
        
        // 创建请求
        ComfyuiRequestDto requestDto = new ComfyuiRequestDto(
            poseReqDto.getClientId() != null ? poseReqDto.getClientId() : Constants.COMFYUI_CLIENT_ID, 
            JSON.parseObject(workflow)
        );
        
        ComfyuiTask task = new ComfyuiTask();
        task.setClientId(poseReqDto.getClientId());
        task.setUserId(UserUtils.getUserId());
        task.setRequestDto(requestDto);
        task.setType("pose");
        
        return task;
    }
    
    private String downloadImageAsBase64(String imageUrl) {
        // TODO: 实现从URL下载图片并转换为Base64
        return "";
    }
}