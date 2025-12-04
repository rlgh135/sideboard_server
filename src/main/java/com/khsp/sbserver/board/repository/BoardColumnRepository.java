package com.khsp.sbserver.board.repository;

import com.khsp.sbserver.board.entity.BoardColumn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardColumnRepository extends JpaRepository<BoardColumn, Long> {
    List<BoardColumn> findAllByOrderBySequenceAsc();
}
