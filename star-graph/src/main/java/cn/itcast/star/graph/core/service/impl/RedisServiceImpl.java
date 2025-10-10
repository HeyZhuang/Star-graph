package cn.itcast.star.graph.core.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.itcast.star.graph.comfyui.client.pojo.ComfyuiTask;
import cn.itcast.star.graph.core.service.RedisService;
import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisServiceImpl implements RedisService {
    private final static String TASK_KEY_PREFIX = "task_";
    private final static String DISTRIBUTED_ID_KEY = "DISTRIBUTED_ID";
    private final static String DISTRIBUTED_QUEUE_KEY = "DISTRIBUTED_QUEUE";
    private final static String RUN_TASK_KEY = "run_task_";

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public ComfyuiTask addQueueTask(ComfyuiTask task) {
        // 获取一个分布式自增主键ID
        Long idx = stringRedisTemplate.opsForValue().increment(DISTRIBUTED_ID_KEY);
        task.setIndex(idx);

        // 把任务存入zset集合
        stringRedisTemplate.opsForZSet().add(DISTRIBUTED_QUEUE_KEY,task.getId(),idx);
        // 把整个任务通过string方式存储到redis
        stringRedisTemplate.opsForValue().set(TASK_KEY_PREFIX+task.getId(), JSON.toJSONString(task));
        return task;
    }

    @Override
    public ComfyuiTask popQueueTask() {
        Long size = stringRedisTemplate.opsForZSet().size(DISTRIBUTED_QUEUE_KEY);
        if(size>0) {
            ZSetOperations.TypedTuple<String> task = stringRedisTemplate.opsForZSet().popMin(DISTRIBUTED_QUEUE_KEY);
            if (task != null && task.getValue() != null) {
                String taskId = task.getValue();
                String json = stringRedisTemplate.opsForValue().get(TASK_KEY_PREFIX + taskId);
                ComfyuiTask comfyuiTask = JSON.parseObject(json, ComfyuiTask.class);
                return comfyuiTask;
            }
        }
        return null;
    }

    @Override
    public void addStartedTask(String promptId, ComfyuiTask task) {
        stringRedisTemplate.opsForValue().set(RUN_TASK_KEY+promptId,JSON.toJSONString(task), Duration.ofMinutes(10));
    }

    @Override
    public ComfyuiTask getStartedTask(String promptId) {
        String json = stringRedisTemplate.opsForValue().get(RUN_TASK_KEY + promptId);
        if(StrUtil.isNotEmpty(json)){
            return JSON.parseObject(json,ComfyuiTask.class);
        }
        return null;
    }

    @Override
    public boolean hasQueueTask() {
        return stringRedisTemplate.opsForZSet().size(DISTRIBUTED_QUEUE_KEY)>0;
    }
}
