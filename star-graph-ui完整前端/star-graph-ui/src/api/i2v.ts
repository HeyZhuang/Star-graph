import request from "@/utils/request";

class Image2VideoAPI {
  static generate(data: any) {
    return request({
      url: "/api/authed/1.0/i2v/generate",
      method: "post",
      data: data,
    });
  }
}

export default Image2VideoAPI;