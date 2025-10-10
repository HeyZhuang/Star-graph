package cn.itcast.star.graph.tongyi.client.api;

import cn.itcast.star.graph.tongyi.client.pojo.TongyiText2ImageRequest;
import cn.itcast.star.graph.tongyi.client.pojo.TongyiText2ImageResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * 通义万相 API 接口定义
 */
public interface TongyiApi {

    /**
     * 提交文生图任务
     * @param authorization API Key (格式: Bearer sk-xxx)
     * @param request 请求参数
     * @return 任务提交响应
     */
    @POST("services/aigc/text2image/image-synthesis")
    Call<TongyiText2ImageResponse> submitText2ImageTask(
            @Header("Authorization") String authorization,
            @Header("X-DashScope-Async") String async,
            @Body TongyiText2ImageRequest request
    );

    /**
     * 查询任务状态和结果
     * @param authorization API Key
     * @param taskId 任务 ID
     * @return 任务结果
     */
    @GET("tasks/{taskId}")
    Call<TongyiText2ImageResponse> getTaskResult(
            @Header("Authorization") String authorization,
            @Path("taskId") String taskId
    );
}
