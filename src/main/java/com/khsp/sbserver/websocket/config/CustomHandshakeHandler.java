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
        User user = (User) attributes.get("LOGIN_USER");

        if (user != null) {
            return new StompPrincipal(user.getEmail());
        }

        return super.determineUser(request, wsHandler, attributes);
    }
}
