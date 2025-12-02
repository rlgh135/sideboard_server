package com.khsp.sbserver.board.service;

import com.khsp.sbserver.board.dto.CardMoveRequest;
import com.khsp.sbserver.board.entity.Card;
import com.khsp.sbserver.board.repository.CardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CardConcurrencyTest {
    @Autowired
    private CardService cardService;

    @Autowired
    private CardRepository cardRepository;

    @Test
    @DisplayName("동시성 이슈 테스트: 3명이 동시에 1번 카드를 옮기려 하면 1명만 성공해야 함.")
    void moveCardConcurrencyTest() throws InterruptedException {
        // 1. 테스트 데이터 준비 (1번 카드 생성/초기화)
        // 실제 DB의 ID나 테스트용으로 하나 생성.
        Card card = new Card("경쟁 카드", 1000.0, 1L);
        Card savedCard = cardRepository.save(card);
        Long targetCardId = savedCard.getId();

        // 2. 스레드 3개 준비 (3명이 동시에 클릭하는 상황)
        int nuberOfThreads = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(nuberOfThreads);
        CountDownLatch countDownLatch = new CountDownLatch(nuberOfThreads);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        for (int i = 0; i < nuberOfThreads; i++) {
            executorService.execute(() -> {
                try {
                    // DTO생성, ID만 중요
                    CardMoveRequest request = new CardMoveRequest(targetCardId, 2L, null, null);
                    // reflection 등으로 필드 세팅이 어렵다면 DTO에 @Setter나 @AllArgsConstructor 잠시 추가 추천
                    // 여기선 편의상 DTO 필드를 public으로 가정하거나,
                    // CardMoveRequest에 생성자를 추가해서 테스트하세요.
                    // (임시: CardMoveRequest에 public 생성자나 Setter가 있다고 가정)
                    /* request.setCardId(targetCardId);
                       request.setTargetColumnId(2L); // 다른 컬럼으로 이동 시도
                    */

                    // ※ 중요: DTO에 Setter가 없다면 테스트를 위해 @Setter를 DTO에 추가해주세요!

                    // 실제 서비스 호출
                    cardService.moveCard(request);
                    successCount.getAndIncrement();
                    System.out.println("성공!");
                } catch (ObjectOptimisticLockingFailureException e) {
                    // 충동 발생! (원하는 상황)
                    failureCount.getAndIncrement();
                    System.out.println("충돌 발생! (버전 불일치");
                } catch (Exception e) {
                    System.out.println("기타 에러: "+e.getMessage());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await(); // 3개 쓰레드가 다 끝날 때까지 대기

        // 4. 검븡
        System.out.println("성공 횟수: " + successCount.get());
        System.out.println("실패(충돌) 횟수: " + failureCount.get());

        // 3명이 덤볐으면 1명만 성공하고 2명은 튕겨야 정상 (낙관적 락)
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(2);

    }
}
