package cn.itcast.star.graph.core.service.impl;

import cn.itcast.star.graph.comfyui.client.pojo.ComfyuiRequestDto;
import cn.itcast.star.graph.comfyui.client.pojo.ComfyuiTask;
import cn.itcast.star.graph.core.common.Constants;
import cn.itcast.star.graph.core.dto.UpscaleReqDto;
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
public class UpscaleServiceImpl implements UpscaleService {

    @Autowired
    FreemarkerService freemarkerService;
    @Autowired
    RedisService redisService;
    @Autowired
    UserFundRecordService userFundRecordService;

    @Override
    public Text2ImageResponeDto upscaleImage(UpscaleReqDto upscaleReqDto) throws Exception {
        // 检查用户余额
        Long userId = UserUtils.getUserId();
        int balance = userFundRecordService.checkBalance(userId);
        if(balance < 3){
            throw new CustomException("余额不足，请充值");
        }

        // 构建ComfyUI任务
        ComfyuiTask comfyuiTask = getComfyuiTask(upscaleReqDto);
        
        // 添加到任务队列
        redisService.addTask(comfyuiTask);
        
        // 扣费（画质提升消耗3个积分）
        userFundRecordService.deductBalance(userId, 3);
        
        Text2ImageResponeDto responeDto = new Text2ImageResponeDto();
        responeDto.setCode("200");
        responeDto.setMsg("画质提升任务已提交");
        return responeDto;
    }

    private ComfyuiTask getComfyuiTask(UpscaleReqDto upscaleReqDto) throws Exception {
        // 创建模型参数
        Map<String, Object> modelParams = new HashMap<>();
        modelParams.put("upscale_factor", upscaleReqDto.getUpscaleFactor());
        modelParams.put("upscale_model", upscaleReqDto.getUpscaleModel());
        
        // 处理输入图片
        String inputImage = upscaleReqDto.getImageBase64();
        if(inputImage == null && upscaleReqDto.getImageUrl() != null){
            // TODO: 从URL下载图片并转为base64
            inputImage = downloadImageAsBase64(upscaleReqDto.getImageUrl());
        }
        modelParams.put("input_image", inputImage);
        
        // 生成ComfyUI工作流
        String workflow = freemarkerService.renderUpscale(modelParams);
        
        // 创建请求
        ComfyuiRequestDto requestDto = new ComfyuiRequestDto(
            upscaleReqDto.getClientId() != null ? upscaleReqDto.getClientId() : Constants.COMFYUI_CLIENT_ID, 
            JSON.parseObject(workflow)
        );
        
        ComfyuiTask task = new ComfyuiTask();
        task.setClientId(upscaleReqDto.getClientId());
        task.setUserId(UserUtils.getUserId());
        task.setRequestDto(requestDto);
        task.setType("upscale");
        
        return task;
    }
    
    private String downloadImageAsBase64(String imageUrl) {
        // TODO: 实现从URL下载图片并转换为Base64
        return "";
    }
}