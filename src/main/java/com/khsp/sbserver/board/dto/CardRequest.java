package com.khsp.sbserver.board.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CardRequest {
    private String title;
    private String content;
    private Long columnId;
}
