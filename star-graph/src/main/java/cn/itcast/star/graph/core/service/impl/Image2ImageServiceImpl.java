package cn.itcast.star.graph.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.itcast.star.graph.comfyui.client.pojo.ComfyuiModel;
import cn.itcast.star.graph.comfyui.client.pojo.ComfyuiRequestDto;
import cn.itcast.star.graph.comfyui.client.pojo.ComfyuiTask;
import cn.itcast.star.graph.core.common.Constants;
import cn.itcast.star.graph.core.dto.Image2ImageReqDto;
import cn.itcast.star.graph.core.dto.Text2ImageResponeDto;
import cn.itcast.star.graph.core.exception.CustomException;
import cn.itcast.star.graph.core.service.*;
import cn.itcast.star.graph.core.utils.UserUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class Image2ImageServiceImpl implements Image2ImageService {

    @Autowired
    OllamaService ollamaService;
    @Autowired
    FreemarkerService freemarkerService;
    @Autowired
    RedisService redisService;
    @Autowired
    UserFundRecordService userFundRecordService;

    @Override
    public Text2ImageResponeDto imageToImage(Image2ImageReqDto image2ImageReqDto) throws Exception {
        // 检查用户余额
        Long userId = UserUtils.getUserId();
        int balance = userFundRecordService.checkBalance(userId);
        if(balance < 5){
            throw new CustomException("余额不足，请充值");
        }

        // 构建ComfyUI任务
        ComfyuiTask comfyuiTask = getComfyuiTask(image2ImageReqDto);
        
        // 添加到任务队列
        redisService.addTask(comfyuiTask);
        
        // 扣费
        userFundRecordService.deductBalance(userId, 5);
        
        Text2ImageResponeDto responeDto = new Text2ImageResponeDto();
        responeDto.setCode("200");
        responeDto.setMsg("图生图任务已提交");
        return responeDto;
    }

    private ComfyuiTask getComfyuiTask(Image2ImageReqDto image2ImageReqDto) throws Exception {
        // 创建模型参数
        Map<String, Object> modelParams = new HashMap<>();
        modelParams.put("model_name", image2ImageReqDto.modelName());
        modelParams.put("scheduler", image2ImageReqDto.scheduler());
        modelParams.put("width", image2ImageReqDto.width());
        modelParams.put("height", image2ImageReqDto.height());
        modelParams.put("sampler_name", image2ImageReqDto.samplerName());
        modelParams.put("steps", image2ImageReqDto.getStep());
        modelParams.put("cfg", image2ImageReqDto.getCfg());
        modelParams.put("seed", image2ImageReqDto.getSeed());
        modelParams.put("strength", image2ImageReqDto.getStrength());
        
        // 处理输入图片
        String inputImage = image2ImageReqDto.getImageBase64();
        if(inputImage == null && image2ImageReqDto.getImageUrl() != null){
            // TODO: 从URL下载图片并转为base64
            inputImage = downloadImageAsBase64(image2ImageReqDto.getImageUrl());
        }
        modelParams.put("input_image", inputImage);
        
        // 转换提示词
        String translatedPrompt = ollamaService.translate(image2ImageReqDto.getPropmt());
        modelParams.put("positive_prompt", "(8k, best quality, masterpiece),(high detailed skin)," + translatedPrompt);
        
        String translatedNegative = ollamaService.translate(image2ImageReqDto.getReverse());
        modelParams.put("negative_prompt", translatedNegative + ",bad face,naked,bad finger,bad arm,bad leg,bad eye");
        
        // 生成ComfyUI工作流
        String workflow = freemarkerService.renderImage2Image(modelParams);
        
        // 创建请求
        ComfyuiRequestDto requestDto = new ComfyuiRequestDto(
            image2ImageReqDto.getClientId() != null ? image2ImageReqDto.getClientId() : Constants.COMFYUI_CLIENT_ID, 
            JSON.parseObject(workflow)
        );
        
        ComfyuiTask task = new ComfyuiTask();
        task.setClientId(image2ImageReqDto.getClientId());
        task.setUserId(UserUtils.getUserId());
        task.setRequestDto(requestDto);
        task.setType("image2image");
        
        return task;
    }
    
    private String downloadImageAsBase64(String imageUrl) {
        // TODO: 实现从URL下载图片并转换为Base64
        // 这里需要使用HTTP客户端下载图片，然后转换为Base64
        return "";
    }
}