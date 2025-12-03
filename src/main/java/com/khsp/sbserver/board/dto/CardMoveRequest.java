package com.khsp.sbserver.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardMoveRequest {
    private Long cardId;
    private Long targetColumnId;
    private Long prevCardId;    // 내 위의 카드. 없다면 null
    private Long nextCardId;    // 내 아래의 카드. 없다면 null

    private String clientUuid;
}
