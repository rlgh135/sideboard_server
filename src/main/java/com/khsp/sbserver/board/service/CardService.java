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
        // 1. 모든 컬럼 조회
        List<BoardColumn> columns = columnRepository.findAllByOrderBySequenceAsc();

        // 2. 모든 카드 조회
        List<Card> allCards = cardRepository.findAllByOrderByPositionAsc();

        // 3. 데이터 조립 (컬럼별 카드 그룹핑)
        return columns.stream().map(col -> {
            List<Card> cardsInColumn = allCards.stream()
                    .filter(card -> card.getColumnId().equals(col.getId()))
                    .toList();
            return new BoardResponse(col.getId(), col.getTitle(), cardsInColumn);
        }).toList();
    }

    // ★ [핵심 비즈니스 로직] 권한 검사
    // 1. 목적지가 'Done'(ID: 3) 컬럼인지 확인 (ID는 DB 상황에 따라 다를 수 있음)
    // 2. 요청자가 'MANAGER'가 아닌지 확인
    @Transactional
    public Double moveCard(CardMoveRequest request, User user) {
        if (request.getTargetColumnId() == 3L && user.getRole() == Role.MEMBER) {
            // (주의: 로직 테스트를 위해 'MEMBER'가 못 가게 해야 하는데,
            //  User Entity 생성 시 MANAGER/MEMBER를 잘 구분했는지 확인 필요.
            //  여기선 "MEMBER 권한인 사람은 Done으로 못 간다"로 작성하겠습니다.)

            if (user.getRole() == Role.MEMBER) {
                throw new IllegalArgumentException("🚫 [권한 없음] 'Done' 컬럼 이동은 매니저 결재가 필요합니다!");
            }
        }

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

    @Transactional(readOnly = true)
    public Card getCards(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("카드가 존재하지 않습니다. ID: " + cardId));
    }


    @Transactional
    public Card updateCard(Long id, CardRequest request) {
        // JPA는 **영속성 컨텍스트(Persistence Context)**라는 녀석이 Entity를 관리합니다.

        /*
        * Dirty Checking이 작동하려면 딱 2가지 조건이 필요합니다.
        * 영속 상태의 Entity여야 한다:
        * new Card()로 내가 방금 만든 객체는 안 됩니다.
        * findById()나 save()를 통해 JPA가 관리 중인 객체여야 합니다.
        * 트랜잭션(@Transactional) 안에서 일어나야 한다:
        * 트랜잭션이 시작되고 끝나는 범위 안에서만 변경 감지가 일어납니다. 서비스 메서드 위에 @Transactional을 꼭 붙여야 하는 이유입니다.
        */
        // 1. DB에서 카드를 조회 (이 순간 JPA가 원본 상태를 '스냅샷'으로 떠놓음)
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
