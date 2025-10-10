package cn.itcast.star.graph.core.job;

import cn.hutool.core.date.DateTime;
import cn.itcast.star.graph.comfyui.client.api.ComfyuiApi;
import cn.itcast.star.graph.comfyui.client.pojo.ComfyuiTask;
import cn.itcast.star.graph.core.service.RedisService;
import cn.itcast.star.graph.core.service.UserFundRecordService;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;

@Component
@Log4j2
public class RunTaskJob {

    final static String SPRING_TASK_LOCK_KEY = "SPRING_TASK_LOCK_KEY";
    public final static String TASK_RUN_SEMAPHORE = "TASK_RUN_SEMAPHORE";

    @Autowired
    ComfyuiApi comfyuiApi;
    @Autowired
    RedisService redisService;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    UserFundRecordService userFundRecordService;

    /**
     * 把任务发送给comfyui
     */
    private void sendTaskToComfyui(){
        ComfyuiTask comfyuiTask = redisService.popQueueTask();
        if(comfyuiTask==null){
            return;
        }
        // 发送到comfyui
        Call<HashMap> addQueueTask = comfyuiApi.addQueueTask(comfyuiTask.getComfyuiRequestDto());
        try {
            Response<HashMap> response = addQueueTask.execute();
            if(response.isSuccessful()) {
                HashMap map = response.body();
                String promptId = (String) map.get("prompt_id");
                comfyuiTask.setPromptId(promptId);
                log.info("添加任务到Comfyui成功：{}",comfyuiTask.getPromptId());
                redisService.addStartedTask(promptId,comfyuiTask);
            }else{
                String error = response.errorBody().string();
                log.error("添加任务到Comfyui错误: {}",error);
                // 归还信号量
                RSemaphore semaphore = redissonClient.getSemaphore(TASK_RUN_SEMAPHORE);
                semaphore.release();
                // 冻结归还
                userFundRecordService.freezeReturn(comfyuiTask.getUserId(),comfyuiTask.getSize());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 每秒执行一次
     */
    @Scheduled(cron="*/1 * * * * ?")
    public void runTask() {
        // 获取锁
        RLock lock = redissonClient.getLock(SPRING_TASK_LOCK_KEY);
        // 占有锁
        if(lock.tryLock()) {
            try {
                if(redisService.hasQueueTask()) {
                    RSemaphore semaphore = redissonClient.getSemaphore(TASK_RUN_SEMAPHORE);
                    // 占用一个信号量，如果占用成功才发送任务
                    if (semaphore.tryAcquire()) {
                        System.out.println("===开关开启：>" + DateTime.now());
                        sendTaskToComfyui();
//              System.out.println("task1" + DateTime.now());
                    }
                }
            }finally {
                lock.unlock();// 释放锁
            }
        }
    }

}
