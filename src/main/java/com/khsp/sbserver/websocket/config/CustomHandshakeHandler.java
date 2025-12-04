package com.khsp.sbserver.websocket.config;

import com.khsp.sbserver.user.entity.User;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // 1. Interceptor에서 넣어둔 값 꺼내기
        User user = (User) attributes.get("LOGIN_USER");

        if (user != null) {
            // 2. 유저의 ID(고유값)을 이용해 principal을 만들어 반환.
            // 소켓 연결의 주인은 user.getEmail()
            return new StompPrincipal(user.getEmail());
        }

        return super.determineUser(request, wsHandler, attributes);
    }
}
