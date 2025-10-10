package cn.itcast.star.graph.core.service.impl;

import cn.itcast.star.graph.core.dto.Text2ImageReqDto;
import cn.itcast.star.graph.core.dto.Text2ImageResponeDto;
import cn.itcast.star.graph.core.service.Text2ImageService;
import cn.itcast.star.graph.core.service.TongyiImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * 混合架构文生图服务
 * 支持根据配置切换不同的 AI 提供商
 */
@Slf4j
@Service
@Primary  // 设置为主要实现，覆盖原有的 Text2ImageServiceImpl
public class HybridText2ImageServiceImpl implements Text2ImageService {

    @Autowired
    private Text2ImageServiceImpl comfyuiService;  // 原有的 ComfyUI 服务

    @Autowired
    private TongyiImageService tongyiService;  // 通义万相服务

    @Value("${ai.provider:comfyui}")
    private String provider;

    @Value("${ai.fallback.enabled:true}")
    private boolean fallbackEnabled;

    @Override
    public Text2ImageResponeDto textToImage(Text2ImageReqDto text2ImageReqDto) throws Exception {
        log.info("混合架构文生图 - 提供商: {}, 降级开关: {}", provider, fallbackEnabled);

        // 根据配置选择服务提供商
        switch (provider.toLowerCase()) {
            case "tongyi":
                return handleTongyiRequest(text2ImageReqDto);

            case "comfyui":
                return handleComfyuiRequest(text2ImageReqDto);

            case "auto":
                // 自动选择：优先通义万相，失败回退到 ComfyUI
                return handleAutoRequest(text2ImageReqDto);

            default:
                log.warn("未知的提供商: {}, 使用默认 ComfyUI", provider);
                return comfyuiService.textToImage(text2ImageReqDto);
        }
    }

    /**
     * 处理通义万相请求
     */
    private Text2ImageResponeDto handleTongyiRequest(Text2ImageReqDto reqDto) throws Exception {
        try {
            log.info("使用通义万相生成图片");
            return tongyiService.textToImage(reqDto);
        } catch (Exception e) {
            log.error("通义万相调用失败", e);

            // 如果启用降级，回退到 ComfyUI
            if (fallbackEnabled) {
                log.info("降级到 ComfyUI");
                return comfyuiService.textToImage(reqDto);
            } else {
                throw e;
            }
        }
    }

    /**
     * 处理 ComfyUI 请求
     */
    private Text2ImageResponeDto handleComfyuiRequest(Text2ImageReqDto reqDto) throws Exception {
        try {
            log.info("使用 ComfyUI 生成图片");
            return comfyuiService.textToImage(reqDto);
        } catch (Exception e) {
            log.error("ComfyUI 调用失败", e);

            // 如果启用降级，回退到通义万相
            if (fallbackEnabled) {
                log.info("降级到通义万相");
                return tongyiService.textToImage(reqDto);
            } else {
                throw e;
            }
        }
    }

    /**
     * 自动选择模式
     */
    private Text2ImageResponeDto handleAutoRequest(Text2ImageReqDto reqDto) throws Exception {
        // 优先使用通义万相（速度快）
        try {
            log.info("自动模式：优先使用通义万相");
            return tongyiService.textToImage(reqDto);
        } catch (Exception e) {
            log.warn("通义万相失败，尝试 ComfyUI", e);
            return comfyuiService.textToImage(reqDto);
        }
    }
}
