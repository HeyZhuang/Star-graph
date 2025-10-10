package cn.itcast.star.graph.core.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.http.HttpUtil;
import cn.itcast.star.graph.comfyui.client.pojo.ComfyuiRequestDto;
import cn.itcast.star.graph.comfyui.client.pojo.ComfyuiTask;
import cn.itcast.star.graph.core.common.Constants;
import cn.itcast.star.graph.core.dto.Image2VideoReqDto;
import cn.itcast.star.graph.core.dto.VideoGenerationResponseDto;
import cn.itcast.star.graph.core.exception.CustomException;
import cn.itcast.star.graph.core.service.*;
import cn.itcast.star.graph.core.utils.UserUtils;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class Image2VideoServiceImpl implements Image2VideoService {

    @Autowired
    private OllamaService ollamaService;
    @Autowired
    private FreemarkerService freemarkerService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private UserFundRecordService userFundRecordService;

    @Override
    public VideoGenerationResponseDto imageToVideo(Image2VideoReqDto image2VideoReqDto) throws Exception {
        // 检查用户余额
        Long userId = UserUtils.getUserId();
        int balance = userFundRecordService.checkBalance(userId);
        int requiredPoints = calculateRequiredPoints(image2VideoReqDto.getDuration());
        
        if(balance < requiredPoints){
            throw new CustomException("余额不足，生成" + image2VideoReqDto.getDuration() + "秒视频需要" + requiredPoints + "积分");
        }

        // 检查输入图片
        if(image2VideoReqDto.getImageBase64() == null && image2VideoReqDto.getImageUrl() == null) {
            throw new CustomException("请提供输入图片");
        }

        // 构建ComfyUI任务
        ComfyuiTask comfyuiTask = getComfyuiTask(image2VideoReqDto);
        
        // 添加到任务队列
        long queueIndex = redisService.addTask(comfyuiTask);
        
        // 扣费
        userFundRecordService.deductBalance(userId, requiredPoints);
        
        VideoGenerationResponseDto responseDto = new VideoGenerationResponseDto();
        responseDto.setCode("200");
        responseDto.setMsg("图生视频任务已提交");
        responseDto.setPid(comfyuiTask.getId());
        responseDto.setQueueIndex((int) queueIndex);
        responseDto.setDuration(image2VideoReqDto.getDuration());
        responseDto.setFps(image2VideoReqDto.getFps());
        
        return responseDto;
    }

    private ComfyuiTask getComfyuiTask(Image2VideoReqDto image2VideoReqDto) throws Exception {
        // 创建模型参数
        Map<String, Object> modelParams = new HashMap<>();
        modelParams.put("model_name", image2VideoReqDto.getModelName());
        modelParams.put("duration", image2VideoReqDto.getDuration());
        modelParams.put("fps", image2VideoReqDto.getFps());
        modelParams.put("motion_bucket_id", image2VideoReqDto.getMotionBucketId());
        modelParams.put("augmentation_level", image2VideoReqDto.getAugmentationLevel());
        modelParams.put("cfg_scale", image2VideoReqDto.getCfgScale());
        modelParams.put("steps", image2VideoReqDto.getSteps());
        modelParams.put("seed", image2VideoReqDto.getSeed());
        modelParams.put("video_loop", image2VideoReqDto.isVideoLoop());
        
        // 处理输入图片
        String inputImage = image2VideoReqDto.getImageBase64();
        if(inputImage == null && image2VideoReqDto.getImageUrl() != null){
            inputImage = downloadImageAsBase64(image2VideoReqDto.getImageUrl());
        }
        modelParams.put("input_image", inputImage);
        
        // 如果提供了描述，添加提示词
        if(image2VideoReqDto.getPropmt() != null && !image2VideoReqDto.getPropmt().isEmpty()) {
            String translatedPrompt = ollamaService.translate(image2VideoReqDto.getPropmt());
            modelParams.put("prompt", translatedPrompt);
        }
        
        // 生成ComfyUI工作流
        String workflow = freemarkerService.renderImage2Video(modelParams);
        
        // 创建请求
        String clientId = image2VideoReqDto.getClientId() != null ? 
            image2VideoReqDto.getClientId() : Constants.COMFYUI_CLIENT_ID;
            
        ComfyuiRequestDto requestDto = new ComfyuiRequestDto(clientId, JSON.parseObject(workflow));
        
        ComfyuiTask task = new ComfyuiTask();
        task.setClientId(clientId);
        task.setUserId(UserUtils.getUserId());
        task.setRequestDto(requestDto);
        task.setType("image2video");
        task.setSize(image2VideoReqDto.getDuration() * image2VideoReqDto.getFps());
        
        return task;
    }
    
    private String downloadImageAsBase64(String imageUrl) {
        try {
            byte[] imageBytes = HttpUtil.downloadBytes(imageUrl);
            return Base64.encode(imageBytes);
        } catch (Exception e) {
            log.error("下载图片失败: {}", imageUrl, e);
            throw new CustomException("下载图片失败");
        }
    }
    
    private int calculateRequiredPoints(int duration) {
        // 图生视频积分计算：基础8分 + 每秒4分
        return 8 + (duration * 4);
    }
}