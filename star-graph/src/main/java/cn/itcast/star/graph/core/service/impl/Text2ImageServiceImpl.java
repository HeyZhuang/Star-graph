package cn.itcast.star.graph.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.itcast.star.graph.comfyui.client.pojo.ComfyuiModel;
import cn.itcast.star.graph.comfyui.client.pojo.ComfyuiRequestDto;
import cn.itcast.star.graph.comfyui.client.pojo.ComfyuiTask;
import cn.itcast.star.graph.core.common.Constants;
import cn.itcast.star.graph.core.dto.Text2ImageReqDto;
import cn.itcast.star.graph.core.dto.Text2ImageResponeDto;
import cn.itcast.star.graph.core.exception.CustomException;
import cn.itcast.star.graph.core.service.*;
import cn.itcast.star.graph.core.utils.UserUtils;
import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Text2ImageServiceImpl implements Text2ImageService {

    @Autowired
    OllamaService ollamaService;
    @Autowired
    FreemarkerService freemarkerService;
    @Autowired
    RedisService redisService;
    @Autowired
    UserFundRecordService userFundRecordService;

    /**
     * 把请求参数封装成comfyui的请求对象
     * @param text2ImageReqDto
     * @return
     * @throws Exception
     */
    public ComfyuiTask getComfyuiTask(Text2ImageReqDto text2ImageReqDto) throws Exception {
        ComfyuiModel comfyuiModel = new ComfyuiModel();
        BeanUtil.copyProperties(text2ImageReqDto,comfyuiModel,true);
        comfyuiModel.setModelName(text2ImageReqDto.modelName());
        comfyuiModel.setScheduler(text2ImageReqDto.scheduler());
        comfyuiModel.setWidth(text2ImageReqDto.width());
        comfyuiModel.setHeight(text2ImageReqDto.height());

        // 转换提示词
        comfyuiModel.setPropmt("(8k, best quality, masterpiece),(high detailed skin),"+ollamaService.translate(text2ImageReqDto.getPropmt()));
        comfyuiModel.setReverse(ollamaService.translate(text2ImageReqDto.getReverse())+",bad face,naked,bad finger,bad arm,bad leg,bad eye");// 负向词

        // 生成comfyui的请求对象
        String propmt = freemarkerService.renderText2Image(comfyuiModel);
        ComfyuiRequestDto requestDto = new ComfyuiRequestDto(Constants.COMFYUI_CLIENT_ID, JSON.parseObject(propmt));

        // 最后构造ComfuiTask对象
        ComfyuiTask comfyuiTask = new ComfyuiTask(text2ImageReqDto.getClientId(), requestDto);
        // 存储用户ID
        comfyuiTask.setUserId(UserUtils.getUser().getId());
        comfyuiTask.setSize(text2ImageReqDto.getSize());
        return comfyuiTask;
    }

    /**
     * 文生图接口实现
     * @param text2ImageReqDto
     * @return
     * @throws Exception
     */
    @Override
    public Text2ImageResponeDto textToImage(Text2ImageReqDto text2ImageReqDto) throws Exception {
        // 把请求参数封装并存储到redis中
        if(text2ImageReqDto.getSize()<1){
            throw new CustomException("请求参数错误！");
        }
        userFundRecordService.pointsFreeze(UserUtils.getUser().getId(),text2ImageReqDto.getSize());
        // 封装数据：
        ComfyuiTask comfyuiTask = getComfyuiTask(text2ImageReqDto);
        // 保存到redis
        comfyuiTask = redisService.addQueueTask(comfyuiTask);
        // 返回数据
        Text2ImageResponeDto text2ImageResponeDto = new Text2ImageResponeDto();
        text2ImageResponeDto.setPid(comfyuiTask.getId());
        text2ImageResponeDto.setQueueIndex(comfyuiTask.getIndex());
        return text2ImageResponeDto;
    }
}
