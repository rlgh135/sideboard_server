package com.khsp.sbserver.board.service;

import com.khsp.sbserver.board.dto.BoardResponse;
import com.khsp.sbserver.board.dto.CardMoveRequest;
import com.khsp.sbserver.board.dto.CardRequest;
import com.khsp.sbserver.board.entity.BoardColumn;
import com.khsp.sbserver.board.entity.Card;
import com.khsp.sbserver.board.repository.BoardColumnRepository;
import com.khsp.sbserver.board.repository.CardRepository;
import com.khsp.sbserver.user.entity.User;
import com.khsp.sbserver.user.enums.Role;
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
    private final BoardColumnRepository columnRepository;

    @Transactional
    public List<BoardResponse> getBoard() {
        List<BoardColumn> columns = columnRepository.findAllByOrderBySequenceAsc();

        List<Card> allCards = cardRepository.findAllByOrderByPositionAsc();

        return columns.stream().map(col -> {
            List<Card> cardsInColumn = allCards.stream()
                    .filter(card -> card.getColumnId().equals(col.getId()))
                    .toList();
            return new BoardResponse(col.getId(), col.getTitle(), cardsInColumn);
        }).toList();
    }

    @Transactional
    public Double moveCard(CardMoveRequest request, User user) {
        if (request.getTargetColumnId() == 3L && user.getRole() == Role.MEMBER) {

            if (user.getRole() == Role.MEMBER) {
                throw new IllegalArgumentException("'Done' 컬럼 이동은 매니저 결재가 필요합니다");
            }
        }

        Card targetCard = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new IllegalArgumentException("카드가 존재하지 않습니다"));

        Double newPosition = calculatePosition(request.getPrevCardId(), request.getNextCardId());

        targetCard.move(request.getTargetColumnId(), newPosition);

        log.info(">>> 카드 이동 완료: ID={}, NewPosition={}", targetCard.getId(), newPosition);

        return newPosition;
    }

    private Double calculatePosition(Long prevCardId, Long nextCardId) {
        Double prevPos = null;
        Double nextPos = null;

        if (prevCardId != null) {
            prevPos = cardRepository.findById(prevCardId)
                    .map(Card::getPosition)
                    .orElse(null);
        }

        if (nextCardId != null) {
            nextPos = cardRepository.findById(nextCardId)
                    .map(Card::getPosition)
                    .orElse(null);
        }

        if (prevPos == null && nextPos == null) {
            return 1000.0;
        } else if (prevPos == null) {
            return nextPos / 2;
        } else if (nextPos == null) {
            return prevPos + 1000.0;
        } else {
            return (prevPos + nextPos) / 2;
        }
    }

    @Transactional(readOnly = true)
    public Card getCards(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("카드가 존재하지 않습니다. ID: " + cardId));
    }


    @Transactional
    public Card updateCard(Long id, CardRequest request) {
        /*
        * 멘티 여러분
        * JPA는 영속성 컨텍스트(Persistence Context)라는 녀석이 Entity를 관리합니다.
        * Dirty Checking이 작동하려면 2가지 조건이 필요합니다.
        * 1. 영속 상태의 Entity여야 한다:
        * new Card()로 내가 방금 만든 객체는 안 됩니다.
        * findById()나 save()를 통해 JPA가 관리 중인 객체여야 합니다.
        * 2. 트랜잭션(@Transactional) 안에서 일어나야 한다:
        * 트랜잭션이 시작되고 끝나는 범위 안에서만 변경 감지가 일어납니다. 서비스 메서드 위에 @Transactional을 꼭 붙여야 하는 이유입니다.
        */
        Card card = getCards(id);

        // 객체의 값만 변경 (DB에 쿼리 안 날림) Dirty Checking으로 자동 업데이트
        card.update(request.getTitle(), request.getContent());

        // save() 호출 안 함
        // 메서드가 끝날 때(트랜잭션 커밋 시점)에 JPA가 알아서 변경된 걸 감지하고 UPDATE 쿼리를 날림
        return card;
    }

    @Transactional
    public Card createCard(CardRequest request) {
        Double maxPosition = cardRepository.findMaxPositionByColumnId(request.getColumnId());
        double newPosition = (maxPosition != null) ? maxPosition + 1024.0 :1024.0;

        Card card = new Card(
                request.getTitle(),
                request.getContent(),
                newPosition,
                request.getColumnId()
        );

        return cardRepository.save(card);
    }
}
