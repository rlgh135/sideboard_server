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

    // 카드가 속한 컬럼 ID (단순화를 위해 연관관계 매핑 대신 ID만 저장, 필요시 @ManyToOne 변경 가능)
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

    // 제목/내용 수정 메서드 (Dirty Checking용)
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // 카드 이동 핵심 메서드
    public void move(Long targetColumnId, Double newPosition) {
        this.columnId = targetColumnId;
        this.position = newPosition;
    }
}
