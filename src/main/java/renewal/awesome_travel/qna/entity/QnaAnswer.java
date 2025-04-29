package renewal.awesome_travel.qna.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import renewal.awesome_travel.config.AuditingFields;

@Entity
@Getter
public class QnaAnswer extends AuditingFields {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long qnaId;
    private Long responderId; // 관리자 or 유저
    private String content;

    public static QnaAnswer create(Long qnaId, Long responderId, String content) {
        QnaAnswer answer = new QnaAnswer();
        answer.qnaId = qnaId;
        answer.responderId = responderId;
        answer.content = content;
        return answer;
    }

    public void updateContent(String content) {
        this.content = content;
    }

}
