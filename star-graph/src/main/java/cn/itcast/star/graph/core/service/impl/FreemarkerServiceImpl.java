package cn.itcast.star.graph.core.service.impl;

import cn.itcast.star.graph.comfyui.client.pojo.ComfyuiModel;
import cn.itcast.star.graph.core.service.FreemarkerService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Service
public class FreemarkerServiceImpl implements FreemarkerService {

    @Autowired
    Configuration configuration;

    @Override
    public String renderText2Image(ComfyuiModel model) throws Exception {
        Template template = configuration.getTemplate("t2i.ftlh");
        Map<String,Object> data = new HashMap<>();
        data.put("config", model);
        StringWriter out = new StringWriter();
//        FileWriter fw = new FileWriter("d:/1.txt");
        /**
         * 第一个参数是，模版要用到的数据
         * 第二个参数是，模版生成的结果存放的位置
         */
        template.process(data,out);//使用数据去替换模版
        return out.toString();
    }
    
    @Override
    public String renderImage2Image(Map<String, Object> params) throws Exception {
        Template template = configuration.getTemplate("i2i.ftlh");
        Map<String,Object> data = new HashMap<>();
        data.put("config", params);
        StringWriter out = new StringWriter();
        template.process(data, out);
        return out.toString();
    }
    
    @Override
    public String renderUpscale(Map<String, Object> params) throws Exception {
        Template template = configuration.getTemplate("upscale.ftlh");
        Map<String,Object> data = new HashMap<>();
        data.put("config", params);
        StringWriter out = new StringWriter();
        template.process(data, out);
        return out.toString();
    }
    
    @Override
    public String renderPose(Map<String, Object> params) throws Exception {
        Template template = configuration.getTemplate("pose.ftlh");
        Map<String,Object> data = new HashMap<>();
        data.put("config", params);
        StringWriter out = new StringWriter();
        template.process(data, out);
        return out.toString();
    }
    
    @Override
    public String renderText2Video(Map<String, Object> params) throws Exception {
        Template template = configuration.getTemplate("t2v.ftlh");
        Map<String,Object> data = new HashMap<>();
        data.put("config", params);
        StringWriter out = new StringWriter();
        template.process(data, out);
        return out.toString();
    }
    
    @Override
    public String renderImage2Video(Map<String, Object> params) throws Exception {
        Template template = configuration.getTemplate("i2v.ftlh");
        Map<String,Object> data = new HashMap<>();
        data.put("config", params);
        StringWriter out = new StringWriter();
        template.process(data, out);
        return out.toString();
    }
}
