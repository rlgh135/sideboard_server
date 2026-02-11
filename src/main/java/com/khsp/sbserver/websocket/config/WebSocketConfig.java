package com.khsp.sbserver.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트(React)가 처음 연결할 주소
        // 예: var socket = new SockJS('/ws');
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // CORS 허용
                .addInterceptors(new HttpSessionHandshakeInterceptor()) // 인터셉터 추가 필수
                .setHandshakeHandler(new CustomHandshakeHandler())
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 구독 요청 url prefix (나중에 React에서 /sub를 앞에 붙여 구독할 수 있음, /queue는 락의 처리 + 에러 핸들링을 위해 사용 )
        registry.enableSimpleBroker("/sub","/queue");

        // 메시지 발행 요청 url prefix (나중에 React에서 /pub를 앞에 붙여 발행할 수 있음)
        registry.setApplicationDestinationPrefixes("/pub");
    }
}