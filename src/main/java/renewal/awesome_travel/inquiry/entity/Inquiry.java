package renewal.awesome_travel.inquiry.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // 문의 작성자

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private boolean isAnswered = false; // 답변 완료 여부

    private LocalDateTime createdAt;

    private LocalDateTime answeredAt; // 답변 완료 시간

    // 문의 생성
    public static Inquiry create(Long userId, String title, String content) {
        Inquiry inquiry = new Inquiry();
        inquiry.userId = userId;
        inquiry.title = title;
        inquiry.content = content;
        inquiry.createdAt = LocalDateTime.now();
        return inquiry;
    }

    public void markAnswered() {
        this.isAnswered = true;
        this.answeredAt = LocalDateTime.now();
    }
}

