package com.khsp.sbserver.board.controller;

import com.khsp.sbserver.board.entity.Card;
import com.khsp.sbserver.board.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CardController {

    private final CardService cardService;

    @GetMapping("/api/cards")
    public ResponseEntity<List<Card>> getCards() {
        return ResponseEntity.ok(cardService.getCards());
    }
}
