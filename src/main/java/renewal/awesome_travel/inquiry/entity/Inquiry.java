package renewal.awesome_travel.inquiry.entity;

import jakarta.persistence.*;
import lombok.Getter;
<<<<<<< Updated upstream
=======
import renewal.awesome_travel.member.entity.User;
>>>>>>> Stashed changes

import java.time.LocalDateTime;

@Entity
@Getter
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

<<<<<<< Updated upstream
    private Long userId; // 문의 작성자
=======
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 문의 작성자
>>>>>>> Stashed changes

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private boolean isAnswered = false; // 답변 완료 여부

    private LocalDateTime createdAt;

    private LocalDateTime answeredAt; // 답변 완료 시간

    // 문의 생성
<<<<<<< Updated upstream
    public static Inquiry create(Long userId, String title, String content) {
        Inquiry inquiry = new Inquiry();
        inquiry.userId = userId;
=======
    public static Inquiry create(User user, String title, String content) {
        Inquiry inquiry = new Inquiry();
        inquiry.user = user;
>>>>>>> Stashed changes
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

