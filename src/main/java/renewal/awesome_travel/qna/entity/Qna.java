package renewal.awesome_travel.qna.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.awesome_travel.config.AuditingFields;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Qna extends AuditingFields {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long writerId; // 유저
    private String title;
    private String content;
    private boolean isAnswered;

    private LocalDateTime createdAt;

    public static Qna create(Long writerId, String title, String content) {
        Qna qna = new Qna();
        qna.writerId = writerId;
        qna.title = title;
        qna.content = content;
        qna.createdAt = LocalDateTime.now();
        return qna;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }


    public void markAnswered() {
        this.isAnswered = true;
    }
}
