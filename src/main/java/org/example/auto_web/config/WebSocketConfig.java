package org.example.auto_web.config;

import org.example.auto_web.websocket.LogWebSocket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;


@Configuration
@EnableWebSocket
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        ServerEndpointExporter exporter = new ServerEndpointExporter();
        logger.info("WebSocket已初始化完成");
        return exporter;
    }

    @Bean
    public LogWebSocket logWebSocket() {
        return LogWebSocket.getInstance();
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WebSocketConfig.class);
}