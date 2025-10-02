import request from "@/utils/request";

class Text2VideoAPI {
  static generate(data: any) {
    return request({
      url: "/api/authed/1.0/t2v/generate",
      method: "post",
      data: data,
    });
  }
}

export default Text2VideoAPI;