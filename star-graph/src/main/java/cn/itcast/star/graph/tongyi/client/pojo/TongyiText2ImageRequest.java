package cn.itcast.star.graph.tongyi.client.pojo;

import lombok.Data;

/**
 * 通义万相文生图请求对象
 */
@Data
public class TongyiText2ImageRequest {

    /**
     * 模型名称，固定为 wanx-v1
     */
    private String model = "wanx-v1";

    /**
     * 输入参数
     */
    private Input input;

    /**
     * 生成参数
     */
    private Parameters parameters;

    @Data
    public static class Input {
        /**
         * 正面提示词
         */
        private String prompt;

        /**
         * 负面提示词
         */
        private String negative_prompt;
    }

    @Data
    public static class Parameters {
        /**
         * 风格：<auto>,<3d cartoon>,<anime>,<oil painting>,<watercolor>,<sketch>,<chinese painting>,<flat illustration>
         */
        private String style = "<auto>";

        /**
         * 图像尺寸：1024*1024, 720*1280, 1280*720
         */
        private String size;

        /**
         * 生成图片数量，取值范围[1,4]
         */
        private Integer n = 1;

        /**
         * 随机种子
         */
        private Long seed;

        /**
         * 参考图 URL（可选）
         */
        private String ref_img;

        /**
         * 参考强度 (0.0-1.0)
         */
        private Double ref_strength;
    }
}
