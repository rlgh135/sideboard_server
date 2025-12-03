package com.khsp.sbserver.board.controller;

import com.khsp.sbserver.board.dto.CardMoveRequest;
import com.khsp.sbserver.board.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;

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
    public void moveCard(@Payload CardMoveRequest request) {
        log.info(">>> [Card Move] ID: {}, TargetColumn: {}, Prev: {}, Next: {}",
                request.getCardId(),
                request.getTargetColumnId(),
                request.getPrevCardId(),
                request.getNextCardId());

        try {
            // 1. 서비스 로직 수행 (DB 업데이트 및 위치 개선)
            Double newPosition = cardService.moveCard(request);

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

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "ERROR");
            errorResponse.put("errorCode", "CONCURRENT_FAIL");
            errorResponse.put("errorMessage", "누군가 먼저 카드를 이동시켰습니다. 최신 정보를 불러옵니다.");
            errorResponse.put("clientUuid", request.getClientUuid());

            messagingTemplate.convertAndSend("/sub/board/1", errorResponse);
        } catch (Exception e) {
            log.error(">>> 카드 이동 중 에러 발생: ", e); // 에러 스택트레이스 남기기
        }
    }
}
