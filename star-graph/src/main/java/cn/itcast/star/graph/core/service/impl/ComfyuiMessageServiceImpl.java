package cn.itcast.star.graph.core.service.impl;

import cn.hutool.core.date.DateTime;
import cn.itcast.star.graph.comfyui.client.pojo.ComfyuiTask;
import cn.itcast.star.graph.comfyui.client.pojo.MessageBase;
import cn.itcast.star.graph.core.job.RunTaskJob;
import cn.itcast.star.graph.core.service.*;
import com.alibaba.fastjson2.JSON;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComfyuiMessageServiceImpl implements ComfyuiMessageService {

    @Autowired
    WsNoticeService wsNoticeService;
    @Autowired
    RedisService redisService;
    @Autowired
    UserResultService userResultService;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    UserFundRecordService userFundRecordService;


    @Override
    public void handleMessage(MessageBase messageBase) {
        if("progress".equals(messageBase.getType())){
            // 进度消息
            handleProgressMessage(messageBase);
        }else if("executed".equals(messageBase.getType())){
            // 生图结果消息
            handleExecutedMessage(messageBase);
        }else if("execution_error".equals(messageBase.getType())){
            // 失败消息
            handleExecutionErrorMessage(messageBase);
        }else if("status".equals(messageBase.getType())){
            handleStatusMessage(messageBase);
        }
    }

    private void handleStatusMessage(MessageBase messageBase) {
        HashMap<String, Object> data = messageBase.getData();
        HashMap<String, Object> status = (HashMap<String, Object>) data.get("status");
        HashMap<String, Object> execInfo = (HashMap<String, Object>) status.get("exec_info");
        int queueRemaining = Integer.valueOf(execInfo.get("queue_remaining")+"");
        if(queueRemaining==0){
            // comfyui中没有要执行的任务，则我开始发送任务给comfyui
            RSemaphore semaphore = redissonClient.getSemaphore(RunTaskJob.TASK_RUN_SEMAPHORE);
            semaphore.release();//归还信号量
            System.out.println(DateTime.now() + "\t\t释放许可数量" + semaphore.availablePermits());
        }
    }

    /**
     * 推送失败消息
     * @param messageBase
     */
    private void handleExecutionErrorMessage(MessageBase messageBase) {
        HashMap<String, Object> data = messageBase.getData();
        String promptId = (String) data.get("prompt_id");
        ComfyuiTask task = redisService.getStartedTask(promptId);
        data.put("type","execution_error");
        if(task==null){
            return;
        }
        userFundRecordService.freezeReturn(task.getUserId(),task.getSize());
        wsNoticeService.sendToUser(task.getWsClientId(), JSON.toJSONString(data));
    }

    /**
     * 推送生图结果
     * @param messageBase
     */
    private void handleExecutedMessage(MessageBase messageBase) {
        // 转化格式
        HashMap<String, Object> data = messageBase.getData();
        HashMap<String, Object> output = (HashMap<String, Object>) data.get("output");
        List<HashMap<String, Object>> images = (List<HashMap<String, Object>>) output.get("images");
        List<String> urls = images.stream().map((image) ->
                        String.format("http://192.168.100.129:8188/view?filename=%s&type=%s&subfolder=", image.get("filename"), image.get("type")))
                .collect(Collectors.toList());
        HashMap<String,Object> temp = new HashMap<>();
        temp.put("type","imageResult");
        temp.put("urls",urls);
        String promptId = (String) data.get("prompt_id");
        ComfyuiTask task = redisService.getStartedTask(promptId);
        if(task==null){
            return;
        }
        userResultService.saveList(urls,task.getUserId());
        wsNoticeService.sendToUser(task.getWsClientId(),JSON.toJSONString(temp));
    }

    private void handleProgressMessage(MessageBase messageBase) {
        HashMap<String, Object> data = messageBase.getData();
        String promptId = (String) data.get("prompt_id");
        ComfyuiTask task = redisService.getStartedTask(promptId);
        data.put("type","progress");
        if(task==null){
            return;
        }
        wsNoticeService.sendToUser(task.getWsClientId(), JSON.toJSONString(data));
    }

}
