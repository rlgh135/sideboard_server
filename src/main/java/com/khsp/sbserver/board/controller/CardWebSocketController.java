package com.khsp.sbserver.board.controller;

import com.khsp.sbserver.board.dto.CardMoveRequest;
import com.khsp.sbserver.board.service.CardService;
import com.khsp.sbserver.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CardWebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final CardService cardService;

    // React에서 /pub/card/move
    @MessageMapping("/card/move")
    public void moveCard(@Payload CardMoveRequest request, SimpMessageHeaderAccessor headerAccessor, Principal principal) {
        log.info(">>> [Card Move] ID: {}, TargetColumn: {}, client: {}",
                request.getCardId(),
                request.getTargetColumnId(),
                request.getClientUuid());

        try {
            // 웹소켓 세션 속성에서 HTTP 세션의 유저 정보 꺼내기 (Interceptor 설정 필요)
            // (WebSocketConfig에서 addInterceptors(new HttpSessionHandshakeInterceptor()) 설정 필요!)
            Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
            User user = (sessionAttributes != null) ? (User) sessionAttributes.get("LOGIN_USER") : null;

            if (user == null) {
                // 실 서비스에서는 연결을 끊거나 메시지 보냄
                throw new IllegalStateException("로그인 세션 만료.");
            }

            log.info(">>> 카드 이동 요청자: {}", user.getNickname());

            // 1. 서비스 로직 수행 (DB 업데이트 및 위치 개선)
            Double newPosition = cardService.moveCard(request, user);

            // 2. 응답 데이터 생성 (변경된 위치 포함)
            Map<String, Object> response = new HashMap<>();
            response.put("type", "SUCCESS"); // 메시지 타입 구분
            response.put("cardId", request.getCardId());
            response.put("columnId", request.getTargetColumnId());
            response.put("newPosition", newPosition);
            response.put("clientUuid", request.getClientUuid());

            // 3. broadcast to subscribers
            // 구독 주소: /sub/board/1 (예시)
            messagingTemplate.convertAndSend("/sub/board/1", response);
            log.debug(">>> 브로드캐스팅 완료: /sub/board/1"); // 디버그 레벨로 기록
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn(">>> 동시성 충돌 발생! Client UUID: {}", request.getClientUuid());
            sendErrorResponse(request, "CONCURRENT_FAIL", "누군가 먼저 카드를 이동시켰습니다. 최신 정보를 불러옵니다.");
        } catch (Exception e) {
            log.error(">>> 카드 이동 중 에러 발생: ", e); // 에러 스택트레이스 남기기
            // 3. ⭐️ 이 유저에게만 에러 메시지 전송 ("/user/queue/errors")
            // principal.getName()은 현재 로그인한 유저의 ID/Email 등 식별자여야 합니다.
            if (principal != null) {
                Map<String, Object> errorPayload = new HashMap<>();
                errorPayload.put("type", "ERROR");
                errorPayload.put("message", e.getMessage());
                errorPayload.put("clientUuid", request.getClientUuid());

                messagingTemplate.convertAndSendToUser(
                        principal.getName(),
                        "/queue/errors",
                        errorPayload // "일반 회원은 이동할 수 없습니다" 텍스트 전송
                );
            }

        }
    }

    // 에러 응답용 헬퍼 메서드 (공용 채널로 쏠 때)
    private void sendErrorResponse(CardMoveRequest request, String code, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("type", "ERROR");
        errorResponse.put("errorCode", code);
        errorResponse.put("errorMessage", message);
        errorResponse.put("clientUuid", request.getClientUuid());

        messagingTemplate.convertAndSend("/sub/board/1", errorResponse);
    }
}
