package com.khsp.sbserver.board.controller;

import com.khsp.sbserver.board.dto.BoardResponse;
import com.khsp.sbserver.board.dto.CardRequest;
import com.khsp.sbserver.board.entity.Card;
import com.khsp.sbserver.board.service.CardService;
import com.khsp.sbserver.global.annotation.LoginUser;
import com.khsp.sbserver.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    // 1. 카드 상세 조회 (모달 열 때 사용)
    @GetMapping("/api/cards/{id}")
    public ResponseEntity<Card> getCards(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCards(id));
    }

    // 2. 카드 등록
    @PostMapping("/api/cards")
    public ResponseEntity<Card> createCard(@RequestBody CardRequest request, @LoginUser User user) {
        if (user == null) {
            throw new IllegalArgumentException("로그인이 필요합니다");
        }
        return ResponseEntity.ok(cardService.createCard(request));
    }

    // 3. 카드 수정
    @PutMapping("/api/cards/{id}")
    public ResponseEntity<Card> updateCard(@PathVariable Long id, @RequestBody CardRequest request, @LoginUser User user) {
        if (user == null) throw new IllegalArgumentException("로그인이 필요합니다.");
        return ResponseEntity.ok(cardService.updateCard(id, request));
    }

    @GetMapping("/api/board")
    public ResponseEntity<List<BoardResponse>> getBoard() {
        return ResponseEntity.ok(cardService.getBoard());
    }

    // 소켓 통신 중 예외가 발생하면 여기로 들어옴
    @MessageExceptionHandler
    @SendToUser("/queue/errors")    // 이 유저에게만 "/user/queue/errors"로 보냄
    public String handleException(Exception e) {
        return e.getMessage();
    }
}
