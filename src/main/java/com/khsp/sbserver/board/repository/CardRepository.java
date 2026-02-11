package com.khsp.sbserver.board.repository;

import com.khsp.sbserver.board.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findAllByOrderByPositionAsc();

    @Query("SELECT MAX(c.position) FROM Card c WHERE c.columnId = :columnId")
    Double findMaxPositionByColumnId(@Param("columnId") Long columnId);
}
