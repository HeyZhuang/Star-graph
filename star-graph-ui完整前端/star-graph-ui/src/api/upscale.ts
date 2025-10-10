import request from "@/utils/request";

class UpscaleAPI {
  static enhance(data: any) {
    return request({
      url: "/api/authed/1.0/upscale/enhance",
      method: "post",
      data: data,
    });
  }
}

export default UpscaleAPI;