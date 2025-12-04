package com.khsp.sbserver.board.dto;

import com.khsp.sbserver.board.entity.Card;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BoardResponse {
    private Long columnId;
    private String title;
    private List<Card> cards;
}
