package com.genersoft.iot.vmp.conf.websocket;

import lombok.extern.slf4j.Slf4j;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 智能分析告警WebSocket端点
 * 用于实时推送告警消息到前端
 */
@ServerEndpoint(value = "/channel/analysis/alarm")
@Slf4j
public class AlarmChannel {

    public static final ConcurrentMap<String, AlarmChannel> CHANNELS = new ConcurrentHashMap<>();

    private Session session;

    @OnMessage(maxMessageSize = 1) // MaxMessage 1 byte
    public void onMessage(String message) {
        try {
            this.session.close(new CloseReason(CloseReason.CloseCodes.TOO_BIG, "此节点不接收任何客户端信息"));
        } catch (IOException e) {
            log.error("[Alarm-WebSocket] 连接关闭失败: id={}, err={}", this.session.getId(), e.getMessage());
        }
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        this.session = session;
        String sessionId = session.getId();
        CHANNELS.put(sessionId, this);
        log.info("[Alarm-WebSocket] 告警推送连接建立: id={}, 当前连接数={}", sessionId, CHANNELS.size());
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        String sessionId = session.getId();
        CHANNELS.remove(sessionId);
        log.info("[Alarm-WebSocket] 告警推送连接断开: id={}, reason={}, 当前连接数={}", 
                sessionId, closeReason.getReasonPhrase(), CHANNELS.size());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        String sessionId = session.getId();
        log.error("[Alarm-WebSocket] 告警推送连接异常: id={}, error={}", sessionId, error.getMessage());
        CHANNELS.remove(sessionId);
    }

    /**
     * 向单个会话发送消息
     */
    public void sendMessage(String message) {
        try {
            if (session != null && session.isOpen()) {
                synchronized (session) {
                    session.getBasicRemote().sendText(message);
                }
            }
        } catch (IOException e) {
            log.error("[Alarm-WebSocket] 发送消息失败: id={}, err={}", session.getId(), e.getMessage());
        }
    }

    /**
     * 向所有连接广播告警消息
     */
    public static void broadcast(String message) {
        log.debug("[Alarm-WebSocket] 广播告警消息: {}", message);
        CHANNELS.values().parallelStream().forEach(channel -> {
            try {
                channel.sendMessage(message);
            } catch (Exception e) {
                log.error("[Alarm-WebSocket] 广播消息到客户端失败: id={}, err={}", 
                        channel.session.getId(), e.getMessage());
            }
        });
    }

    /**
     * 获取当前连接数
     */
    public static int getConnectionCount() {
        return CHANNELS.size();
    }
}