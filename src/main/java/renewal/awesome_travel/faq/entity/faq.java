package renewal.awesome_travel.faq.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.awesome_travel.config.AuditingFields;
import renewal.awesome_travel.faq.utils.FaqCategory;

@Entity
@Getter
@NoArgsConstructor
public class Faq extends AuditingFields {
    @Id
    @GeneratedValue
    private Long id;
    private String question;
    private String answer;
    @Enumerated(EnumType.STRING)
    private FaqCategory category;

    // 생성 메서드
    public Faq (String question, String answer, FaqCategory category) {
        this.question = question;
        this.answer = answer;
        this.category = category;
    }

    // 수정 메서드
    public void update(String question, String answer, FaqCategory category) {
        this.question = question;
        this.answer = answer;
        this.category = category;
    }
}

