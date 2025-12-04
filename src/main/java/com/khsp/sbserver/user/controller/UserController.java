package com.khsp.sbserver.user.controller;

import com.khsp.sbserver.user.dto.LoginRequest;
import com.khsp.sbserver.user.entity.User;
import com.khsp.sbserver.user.repository.UserRepositoty;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // ★ 중요: 인증 쿠키 허용
public class UserController {
    private final UserRepositoty userRepositoty;

    // 로그인
    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        User user = userRepositoty.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }

        // 세션 생성 및 유저 정보 저장
        HttpSession session = httpRequest.getSession();
        session.setAttribute("LOGIN_USER", user);
        session.setMaxInactiveInterval(3600);   // 세션 1시간 유지

        return ResponseEntity.ok("로그인 성공. 환영합니다 " + user.getNickname() + "님.");
    }

    // 로그아웃
    @PostMapping("/api/logout")
    public ResponseEntity<String> logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if(session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    // 내 정보 확인 (새로고침 시 로그인 유지 확인용)
    @GetMapping("/api/me")
    public ResponseEntity<User> getMyInfo(@SessionAttribute(name = "LOGIN_USER", required = false) User loginUser) {
        if(loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(loginUser);
    }
}
