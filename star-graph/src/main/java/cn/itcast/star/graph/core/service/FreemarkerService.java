package cn.itcast.star.graph.core.service;

import cn.itcast.star.graph.comfyui.client.pojo.ComfyuiModel;
import java.util.Map;

public interface FreemarkerService {

    /**
     * 返回替换好的comfyui字符串
     * @param model
     * @return
     * @throws Exception
     */
    public String renderText2Image(ComfyuiModel model) throws Exception ;
    
    /**
     * 渲染图生图工作流
     * @param params
     * @return
     * @throws Exception
     */
    public String renderImage2Image(Map<String, Object> params) throws Exception;
    
    /**
     * 渲染画质提升工作流
     * @param params
     * @return
     * @throws Exception
     */
    public String renderUpscale(Map<String, Object> params) throws Exception;
    
    /**
     * 渲染姿势控制工作流
     * @param params
     * @return
     * @throws Exception
     */
    public String renderPose(Map<String, Object> params) throws Exception;
    
    /**
     * 渲染文生视频工作流
     * @param params
     * @return
     * @throws Exception
     */
    public String renderText2Video(Map<String, Object> params) throws Exception;
    
    /**
     * 渲染图生视频工作流
     * @param params
     * @return
     * @throws Exception
     */
    public String renderImage2Video(Map<String, Object> params) throws Exception;

}
