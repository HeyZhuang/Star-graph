package cn.itcast.star.graph.core.service.impl;

import cn.itcast.star.graph.comfyui.client.pojo.ComfyuiRequestDto;
import cn.itcast.star.graph.comfyui.client.pojo.ComfyuiTask;
import cn.itcast.star.graph.core.common.Constants;
import cn.itcast.star.graph.core.dto.Text2VideoReqDto;
import cn.itcast.star.graph.core.dto.VideoGenerationResponseDto;
import cn.itcast.star.graph.core.exception.CustomException;
import cn.itcast.star.graph.core.service.*;
import cn.itcast.star.graph.core.utils.UserUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class Text2VideoServiceImpl implements Text2VideoService {

    @Autowired
    private OllamaService ollamaService;
    @Autowired
    private FreemarkerService freemarkerService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private UserFundRecordService userFundRecordService;

    @Override
    public VideoGenerationResponseDto textToVideo(Text2VideoReqDto text2VideoReqDto) throws Exception {
        // 检查用户余额（视频生成消耗更多积分）
        Long userId = UserUtils.getUserId();
        int balance = userFundRecordService.checkBalance(userId);
        int requiredPoints = calculateRequiredPoints(text2VideoReqDto.getDuration());
        
        if(balance < requiredPoints){
            throw new CustomException("余额不足，生成" + text2VideoReqDto.getDuration() + "秒视频需要" + requiredPoints + "积分");
        }

        // 构建ComfyUI任务
        ComfyuiTask comfyuiTask = getComfyuiTask(text2VideoReqDto);
        
        // 添加到任务队列
        long queueIndex = redisService.addTask(comfyuiTask);
        
        // 扣费
        userFundRecordService.deductBalance(userId, requiredPoints);
        
        VideoGenerationResponseDto responseDto = new VideoGenerationResponseDto();
        responseDto.setCode("200");
        responseDto.setMsg("文生视频任务已提交");
        responseDto.setPid(comfyuiTask.getId());
        responseDto.setQueueIndex((int) queueIndex);
        responseDto.setDuration(text2VideoReqDto.getDuration());
        responseDto.setFps(text2VideoReqDto.getFps());
        
        return responseDto;
    }

    private ComfyuiTask getComfyuiTask(Text2VideoReqDto text2VideoReqDto) throws Exception {
        // 创建模型参数
        Map<String, Object> modelParams = new HashMap<>();
        modelParams.put("model_name", text2VideoReqDto.getModelName());
        modelParams.put("width", text2VideoReqDto.getWidth());
        modelParams.put("height", text2VideoReqDto.getHeight());
        modelParams.put("duration", text2VideoReqDto.getDuration());
        modelParams.put("fps", text2VideoReqDto.getFps());
        modelParams.put("motion_bucket_id", text2VideoReqDto.getMotionBucketId());
        modelParams.put("cfg_scale", text2VideoReqDto.getCfgScale());
        modelParams.put("steps", text2VideoReqDto.getSteps());
        modelParams.put("seed", text2VideoReqDto.getSeed());
        
        // 转换提示词
        String translatedPrompt = ollamaService.translate(text2VideoReqDto.getPropmt());
        modelParams.put("positive_prompt", "(best quality, high quality, masterpiece)," + translatedPrompt);
        
        String translatedNegative = "";
        if(text2VideoReqDto.getNegativePrompt() != null) {
            translatedNegative = ollamaService.translate(text2VideoReqDto.getNegativePrompt());
        }
        modelParams.put("negative_prompt", translatedNegative + ",low quality,bad quality,blurry");
        
        // 生成ComfyUI工作流
        String workflow = freemarkerService.renderText2Video(modelParams);
        
        // 创建请求
        String clientId = text2VideoReqDto.getClientId() != null ? 
            text2VideoReqDto.getClientId() : Constants.COMFYUI_CLIENT_ID;
            
        ComfyuiRequestDto requestDto = new ComfyuiRequestDto(clientId, JSON.parseObject(workflow));
        
        ComfyuiTask task = new ComfyuiTask();
        task.setClientId(clientId);
        task.setUserId(UserUtils.getUserId());
        task.setRequestDto(requestDto);
        task.setType("text2video");
        task.setSize(text2VideoReqDto.getDuration() * text2VideoReqDto.getFps()); // 总帧数
        
        return task;
    }
    
    private int calculateRequiredPoints(int duration) {
        // 视频生成积分计算：基础10分 + 每秒5分
        return 10 + (duration * 5);
    }
}