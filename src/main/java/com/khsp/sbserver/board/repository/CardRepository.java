package com.khsp.sbserver.board.repository;

import com.khsp.sbserver.board.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    // Crud
    List<Card> findAllByOrderByPositionAsc();

    // [추가] 특정 컬럼에서 가장 큰 position 값 찾기 (새 카드 추가용)
    @Query("SELECT MAX(c.position) FROM Card c WHERE c.columnId = :columnId")
    Double findMaxPositionByColumnId(@Param("columnId") Long columnId);
}
