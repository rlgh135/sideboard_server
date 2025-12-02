package com.khsp.sbserver.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트(React)가 처음 연결할 주소
        // 예: var socket = new SockJS('/ws');
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // CORS 허용
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 구독 요청 url prefix (나중에 React에서 /sub/board/1 로 구독)
        registry.enableSimpleBroker("/sub");

        // 메시지 발행 요청 url prefix (나중에 React에서 /pub/card/move 로 전송)
        registry.setApplicationDestinationPrefixes("/pub");
    }
}