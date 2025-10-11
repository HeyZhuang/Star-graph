import request from "@/utils/request";

class Text2ImageAPI {

    /**
     * 添加生成任务
     *
     * @param data {LoginData}
     * @returns
     */
    static propmt(data) {
        return request<any>({
            url: "/api/authed/1.0/t2i/propmt",
            method: "post",
            data: data
        });
    }

    /**
     * 取消任务
     *
     * @param data {LoginData}
     * @returns
     */
    static canelGen(data) {
        return request<any>({
            url: "/api/authed/1.0/t2i/cancel",
            method: "post",
            data: data
        });
    }

    /**
     * 优先任务
     *
     * @param data {LoginData}
     * @returns
     */
    static proprityTask(data) {
        return request<any>({
            url: "/api/authed/1.0/t2i/priority",
            method: "post",
            data: data
        });
    }

    /**
     * 加载图片列表
     *
     * @param data {LoginData}
     * @returns
     */
    static listImages(data) {
        return request<any>({
            url: "/api/authed/1.0/t2i/list",
            method: "post",
            params: data  // 使用 params 而不是 data，将参数放在 URL 查询字符串中
        });
    }
}

export default Text2ImageAPI;
