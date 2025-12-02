package com.khsp.sbserver.board.controller;

import com.khsp.sbserver.board.dto.CardMoveRequest;
import com.khsp.sbserver.board.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
            response.put("cardId", request.getCardId());
            response.put("columnId", request.getTargetColumnId());
            response.put("newPosition", newPosition);
            response.put("newPosition", newPosition);
            response.put("movedBy", "UserA");   // 추후 실제 유저 닉네임

            // 3. broadcast to subscribers
            // 구독 주소: /sub/board/1 (예시)
            messagingTemplate.convertAndSend("/sub/board/1", request);
            log.debug(">>> 브로드캐스팅 완료: /sub/board/1"); // 디버그 레벨로 기록
        } catch (Exception e) {
            log.error(">>> 카드 이동 중 에러 발생: ", e); // 에러 스택트레이스 남기기
        }
    }
}
