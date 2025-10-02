package cn.itcast.star.graph.comfyui.client.pojo;

import lombok.Data;

import java.util.HashMap;

@Data
public class MessageBase {
    private String type;
    private HashMap<String, Object> data;
}
