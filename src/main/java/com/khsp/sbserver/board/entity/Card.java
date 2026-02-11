package com.khsp.sbserver.board.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Card {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(nullable = false)
    private Double position;

    private Long columnId;

    // 긴 본문 내용 저장을위해 @Lob 사용 (MySQL의 TEXT 타입 매핑)
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Version
    private Long version;

    public Card(String title, String content, Double position, Long columnId) {
        this.title = title;
        this.content = content;
        this.position = position;
        this.columnId = columnId;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void move(Long targetColumnId, Double newPosition) {
        this.columnId = targetColumnId;
        this.position = newPosition;
    }
}
