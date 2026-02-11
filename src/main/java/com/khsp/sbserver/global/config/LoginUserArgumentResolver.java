package com.khsp.sbserver.global.config;

import com.khsp.sbserver.global.annotation.LoginUser;
import com.khsp.sbserver.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    // 이 resolver가 동작할 조건: 파라미터에 @LoginUser가 있고, 타입이 User.class인가?
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean isLoginUserAnnotation = parameter.getParameterAnnotation(LoginUser.class) != null;
        boolean isUserClass = User.class.equals(parameter.getParameterType());
        return isLoginUserAnnotation && isUserClass;
    }

    // 조건이 맞으면 세션에서 LOGIN_USER를 꺼내서 반환
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest)  webRequest.getNativeRequest();
        HttpSession session = request.getSession(false);

        if(session == null) {
            return null;
        }

        return session.getAttribute("LOGIN_USER");
    }
}
