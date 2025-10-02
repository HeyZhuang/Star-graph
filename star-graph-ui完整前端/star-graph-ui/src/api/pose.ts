import request from "@/utils/request";

class PoseAPI {
  static generate(data: any) {
    return request({
      url: "/api/authed/1.0/pose/generate",
      method: "post",
      data: data,
    });
  }
}

export default PoseAPI;