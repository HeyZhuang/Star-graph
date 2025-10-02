package cn.itcast.star.graph.comfyui.client.handler;

import cn.itcast.star.graph.comfyui.client.pojo.MessageBase;
import cn.itcast.star.graph.core.service.ComfyuiMessageService;
import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ComfyuiMessageHandler extends TextWebSocketHandler {

    @Autowired
    ComfyuiMessageService comfyuiMessageService;

    /**
     * 一个新的链接连接成功之后处理的逻辑
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("=============连接成功");
    }

    /**
     * 收到消息之后如何处理
     * @param session
     * @param message
     * @throws Exception
     */
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        MessageBase messageBase = JSON.parseObject(payload, MessageBase.class);
        comfyuiMessageService.handleMessage(messageBase);

        System.out.println("收到消息："+ JSON.toJSONString(message));
    }

}
