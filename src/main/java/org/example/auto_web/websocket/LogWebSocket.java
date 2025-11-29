package org.example.auto_web.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// 关键1：移除@Component（避免Spring重复实例化，JSR-356会自己管理实例）
// 关键2：移除SpringConfigurator配置
@ServerEndpoint("/ws/logs")
public class LogWebSocket {

    private static final Logger logger = LoggerFactory.getLogger(LogWebSocket.class);
    private static final Set<Session> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // 静态单例（确保JSR-356和Spring能共用同一个实例）
    private static LogWebSocket instance;

    // 初始化单例
    public LogWebSocket() {
        instance = this;
    }

    // 对外提供单例获取方法（方便Spring其他组件调用broadcastLog）
    public static LogWebSocket getInstance() {
        if (instance == null) {
            synchronized (LogWebSocket.class) {
                if (instance == null) {
                    instance = new LogWebSocket();
                }
            }
        }
        return instance;
    }

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        logger.info("🎯 WebSocket 连接建立成功! ID: {}, 总连接数: {}", session.getId(), sessions.size());
        sendMessage(session, "🔗 WebSocket 连接成功！");
    }

    @OnClose
    public void onClose(Session session) {
        boolean removed = sessions.remove(session);
        if (removed) {
            logger.info("❌ WebSocket 连接关闭: {}, 剩余连接数: {}", session.getId(), sessions.size());
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        sessions.remove(session);
        logger.error("💥 WebSocket 错误: {}, 连接ID: {}", error.getMessage(), session.getId(), error);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("📨 收到客户端消息: {} 来自连接ID: {}", message, session.getId());
        sendMessage(session, "✅ 服务端已收到消息: " + message);
    }

    // 静态方法，支持外部调用
    private static void sendMessage(Session session, String message) {
        if (session == null || !session.isOpen()) {
            logger.warn("⚠️ 会话已关闭，跳过消息发送");
            return;
        }
        session.getAsyncRemote().sendText(message, new SendHandler() {
            @Override
            public void onResult(SendResult result) {
                if (result.isOK()) {
                    logger.debug("✅ 消息发送成功: {} 到连接ID: {}", message, session.getId());
                } else {
                    logger.error("❌ 发送消息失败: {}", result.getException().getMessage());
                }
            }
        });
    }

    // 改为实例方法，通过单例调用
    public void broadcastLog(String logMessage) {
        if (logMessage == null || logMessage.isEmpty()) {
            logger.warn("⚠️ 广播消息为空，跳过发送");
            return;
        }
        logger.info("📢 开始广播消息: {}, 当前活跃连接数: {}", logMessage, sessions.size());

        if (sessions.isEmpty()) {
            logger.warn("⚠️ 没有活跃的 WebSocket 连接，广播终止");
            return;
        }

        for (Session session : sessions.toArray(new Session[0])) {
            sendMessage(session, logMessage);
        }
    }

    // 静态包装方法（兼容原有调用方式）
    public static void broadcast(String logMessage) {
        getInstance().broadcastLog(logMessage);
    }

    public static int getConnectionCount() {
        return sessions.size();
    }
}