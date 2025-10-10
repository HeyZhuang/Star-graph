package cn.itcast.star.graph.core.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.itcast.star.graph.core.dto.Text2ImageReqDto;
import cn.itcast.star.graph.core.dto.Text2ImageResponeDto;
import cn.itcast.star.graph.core.exception.CustomException;
import cn.itcast.star.graph.core.service.*;
import cn.itcast.star.graph.core.utils.UserUtils;
import cn.itcast.star.graph.tongyi.client.api.TongyiApi;
import cn.itcast.star.graph.tongyi.client.pojo.TongyiText2ImageRequest;
import cn.itcast.star.graph.tongyi.client.pojo.TongyiText2ImageResponse;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 通义万相图像生成服务实现
 */
@Slf4j
@Service
public class TongyiImageServiceImpl implements TongyiImageService {

    @Autowired
    private TongyiApi tongyiApi;

    @Autowired
    private OllamaService ollamaService;

    @Autowired
    private WsNoticeService wsNoticeService;

    @Autowired
    private UserResultService userResultService;

    @Autowired
    private UserFundRecordService userFundRecordService;

    @Value("${tongyi.api-key}")
    private String apiKey;

    @Override
    public Text2ImageResponeDto textToImage(Text2ImageReqDto reqDto) throws Exception {
        // 1. 参数验证
        if (reqDto.getSize() < 1 || reqDto.getSize() > 4) {
            throw new CustomException("生成数量必须在 1-4 之间");
        }

        // 2. 积分冻结
        Long userId = UserUtils.getUser().getId();
        userFundRecordService.pointsFreeze(userId, reqDto.getSize());

        // 3. 翻译提示词（可选，通义万相支持中文）
        String positivePrompt = ollamaService.translate(reqDto.getPropmt());
        String negativePrompt = ollamaService.translate(reqDto.getReverse());

        log.info("通义万相文生图 - 用户ID: {}, 提示词: {}", userId, positivePrompt);

        // 4. 构建请求
        TongyiText2ImageRequest request = new TongyiText2ImageRequest();

        TongyiText2ImageRequest.Input input = new TongyiText2ImageRequest.Input();
        input.setPrompt(positivePrompt);
        input.setNegative_prompt(negativePrompt);
        request.setInput(input);

        TongyiText2ImageRequest.Parameters parameters = new TongyiText2ImageRequest.Parameters();
        parameters.setSize(reqDto.width() + "*" + reqDto.height());
        parameters.setN(reqDto.getSize());
        parameters.setSeed(reqDto.seed());
        request.setParameters(parameters);

        // 5. 异步调用 API
        String taskId = IdUtil.fastSimpleUUID();
        CompletableFuture.runAsync(() -> {
            try {
                // 提交任务（异步模式）
                Response<TongyiText2ImageResponse> response = tongyiApi.submitText2ImageTask(
                        "Bearer " + apiKey,
                        "enable",  // 启用异步模式
                        request
                ).execute();

                if (!response.isSuccessful()) {
                    handleError(reqDto.getClientId(), userId, reqDto.getSize(),
                               "API 调用失败: " + response.code());
                    return;
                }

                TongyiText2ImageResponse submitResponse = response.body();
                if (submitResponse == null || submitResponse.getOutput() == null) {
                    handleError(reqDto.getClientId(), userId, reqDto.getSize(), "响应为空");
                    return;
                }

                String remoteTaskId = submitResponse.getOutput().getTask_id();
                log.info("通义万相任务提交成功 - TaskID: {}", remoteTaskId);

                // 轮询任务状态
                pollTaskResult(remoteTaskId, reqDto.getClientId(), userId, reqDto.getSize());

            } catch (Exception e) {
                log.error("通义万相调用异常", e);
                handleError(reqDto.getClientId(), userId, reqDto.getSize(),
                           "生成失败: " + e.getMessage());
            }
        });

        // 6. 立即返回响应
        Text2ImageResponeDto responseDto = new Text2ImageResponeDto();
        responseDto.setPid(taskId);
        responseDto.setQueueIndex(0L); // 通义万相无队列，直接处理
        return responseDto;
    }

    /**
     * 轮询任务结果
     */
    private void pollTaskResult(String taskId, String clientId, Long userId, Integer imageCount) {
        int maxRetries = 60; // 最多轮询 60 次（约 1 分钟）
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                Thread.sleep(1000); // 每秒轮询一次

                Response<TongyiText2ImageResponse> response = tongyiApi.getTaskResult(
                        "Bearer " + apiKey,
                        taskId
                ).execute();

                if (!response.isSuccessful()) {
                    log.error("查询任务失败: {}", response.code());
                    continue;
                }

                TongyiText2ImageResponse result = response.body();
                if (result == null || result.getOutput() == null) {
                    continue;
                }

                String status = result.getOutput().getTask_status();
                log.info("任务状态: {} - TaskID: {}", status, taskId);

                // 发送进度消息
                sendProgressMessage(clientId, retryCount, maxRetries);

                if ("SUCCEEDED".equals(status)) {
                    // 成功：提取图片 URL 并推送
                    List<String> imageUrls = result.getOutput().getResults().stream()
                            .map(TongyiText2ImageResponse.ImageResult::getUrl)
                            .collect(Collectors.toList());

                    // 保存结果
                    userResultService.saveList(imageUrls, userId);

                    // 扣除积分
                    userFundRecordService.pointsDeduct(userId, imageCount);

                    // 推送结果
                    Map<String, Object> resultMsg = new HashMap<>();
                    resultMsg.put("type", "imageResult");
                    resultMsg.put("urls", imageUrls);
                    wsNoticeService.sendToUser(clientId, JSON.toJSONString(resultMsg));

                    log.info("通义万相生成成功 - 用户: {}, 图片数: {}", userId, imageUrls.size());
                    return;

                } else if ("FAILED".equals(status)) {
                    // 失败：归还积分
                    String errorMsg = result.getOutput().getMessage();
                    handleError(clientId, userId, imageCount, "生成失败: " + errorMsg);
                    return;
                }

                retryCount++;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                handleError(clientId, userId, imageCount, "任务中断");
                return;
            } catch (Exception e) {
                log.error("轮询任务异常", e);
                retryCount++;
            }
        }

        // 超时
        handleError(clientId, userId, imageCount, "生成超时，请稍后重试");
    }

    /**
     * 发送进度消息
     */
    private void sendProgressMessage(String clientId, int current, int total) {
        Map<String, Object> progressMsg = new HashMap<>();
        progressMsg.put("type", "progress");
        progressMsg.put("value", current);
        progressMsg.put("max", total);
        wsNoticeService.sendToUser(clientId, JSON.toJSONString(progressMsg));
    }

    /**
     * 错误处理
     */
    private void handleError(String clientId, Long userId, Integer imageCount, String errorMessage) {
        log.error("通义万相生成失败 - 用户: {}, 错误: {}", userId, errorMessage);

        // 归还冻结积分
        userFundRecordService.freezeReturn(userId, imageCount);

        // 推送错误消息
        Map<String, Object> errorMsg = new HashMap<>();
        errorMsg.put("type", "execution_error");
        errorMsg.put("message", errorMessage);
        wsNoticeService.sendToUser(clientId, JSON.toJSONString(errorMsg));
    }
}
