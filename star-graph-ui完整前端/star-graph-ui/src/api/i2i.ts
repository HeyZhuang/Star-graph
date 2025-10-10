import request from "@/utils/request";

class Image2ImageAPI {
  static generate(data: any) {
    return request({
      url: "/api/authed/1.0/i2i/generate",
      method: "post",
      data: data,
    });
  }
}

export default Image2ImageAPI;