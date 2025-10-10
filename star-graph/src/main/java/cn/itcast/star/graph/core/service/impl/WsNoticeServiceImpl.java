package cn.itcast.star.graph.core.service.impl;

import cn.itcast.star.graph.core.service.WsNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WsNoticeServiceImpl implements WsNoticeService {

    public final static String COMFYUI_QUEUE_TOPIC = "/topic/messages";

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void sendToUser(String clientId, String message) {
        simpMessagingTemplate.convertAndSendToUser(clientId,COMFYUI_QUEUE_TOPIC,message);
    }

    @Override
    public void sendToAll(String message) {
        simpMessagingTemplate.convertAndSend(COMFYUI_QUEUE_TOPIC,message);
    }

}
