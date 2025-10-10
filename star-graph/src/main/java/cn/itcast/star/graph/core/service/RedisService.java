package cn.itcast.star.graph.core.service;

import cn.itcast.star.graph.comfyui.client.pojo.ComfyuiTask;

public interface RedisService {

    // 添加一个任务
    public ComfyuiTask addQueueTask(ComfyuiTask task);

    // 从队列zset中取出一个任务
    public ComfyuiTask popQueueTask();

    // 添加开始的任务
    void addStartedTask(String promptId, ComfyuiTask task);

    // 通过promptId获取任务
    ComfyuiTask getStartedTask(String promptId);

    // 判断队列中是否有任务
    public boolean hasQueueTask();
}
