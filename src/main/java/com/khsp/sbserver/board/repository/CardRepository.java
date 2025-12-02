package com.khsp.sbserver.board.repository;

import com.khsp.sbserver.board.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {
    // Crud

}
