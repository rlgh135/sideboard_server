package com.khsp.sbserver.board.service;

import com.khsp.sbserver.board.dto.CardMoveRequest;
import com.khsp.sbserver.board.entity.Card;
import com.khsp.sbserver.board.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;

    @Transactional
    public List<Card> getCards() {
        return cardRepository.findAllByOrderByPositionAsc();
    }

    @Transactional
    public Double moveCard(CardMoveRequest request) {
        // 1. 이동할 카드 찾기
        Card targetCard = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new IllegalArgumentException("카드가 존재하지 않습니다"));

        // 2. 위치 계산 로직
        Double newPosition = calculatePosition(request.getPrevCardId(), request.getNextCardId());

        // 3. 카드 정보 업데이트
        targetCard.move(request.getTargetColumnId(), newPosition);

        log.info(">>> 카드 이동 완료: ID={}, NewPosition={}", targetCard.getId(), newPosition);

        return newPosition;
    }

    // [면접 포인트] 앞뒤 카드의 위치를 기반으로 중간값 계산
    private Double calculatePosition(Long prevCardId, Long nextCardId) {
        Double prevPos = null;
        Double nextPos = null;

        // 1. 앞 카드가 있으면 위치 가져오기
        if (prevCardId != null) {
            prevPos = cardRepository.findById(prevCardId)
                    .map(Card::getPosition)
                    .orElse(null);
        }

        // 2. 뒤 카드가 있으면 위치 가져오기
        if (nextCardId != null) {
            nextPos = cardRepository.findById(nextCardId)
                    .map(Card::getPosition)
                    .orElse(null);
        }

        // 3. 경우의 수에 따른 계산 (1024는 임의의 간격 기준)
        if (prevPos == null && nextPos == null) {
            // Case A: 빈 컬럼으로 이동 (기준점)
            return 1000.0;
        } else if (prevPos == null) {
            // Case B: 맨 위로 이동 (다음 카드의 절반)
            return nextPos / 2;
        } else if (nextPos == null) {
            // Case C: 맨 아래로 이동 (앞 카드 + 1000)
            return prevPos + 1000.0;
        } else {
            // Case D: 두 카드 사이로 이동 (중간값)
            return (prevPos + nextPos) / 2;
        }
    }
}
