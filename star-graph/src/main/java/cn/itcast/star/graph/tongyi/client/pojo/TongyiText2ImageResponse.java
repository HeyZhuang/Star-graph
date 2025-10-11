package cn.itcast.star.graph.tongyi.client.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * 通义万相文生图响应对象
 */
@Data
public class TongyiText2ImageResponse {

    /**
     * 请求 ID
     */
    private String request_id;

    /**
     * 输出结果
     */
    private Output output;

    /**
     * 使用量信息
     */
    private Usage usage;

    @Data
    public static class Output {
        /**
         * 任务 ID
         */
        private String task_id;

        /**
         * 任务状态：PENDING, RUNNING, SUCCEEDED, FAILED
         */
        private String task_status;

        /**
         * 生成的图片结果
         */
        private List<ImageResult> results;

        /**
         * 任务提交时间
         */
        private String submit_time;

        /**
         * 任务调度时间
         */
        private String scheduled_time;

        /**
         * 任务结束时间
         */
        private String end_time;

        /**
         * 错误码
         */
        private String code;

        /**
         * 错误信息
         */
        private String message;

        /**
         * 任务指标
         */
        private TaskMetrics task_metrics;
    }

    @Data
    public static class TaskMetrics {
        /**
         * 总数
         */
        @JsonProperty("TOTAL")
        private Integer total;

        /**
         * 成功数
         */
        @JsonProperty("SUCCEEDED")
        private Integer succeeded;

        /**
         * 失败数
         */
        @JsonProperty("FAILED")
        private Integer failed;
    }

    @Data
    public static class ImageResult {
        /**
         * 图片 URL（临时链接，24小时有效）
         */
        private String url;
    }

    @Data
    public static class Usage {
        /**
         * 图片数量
         */
        private Integer image_count;
    }
}
