package renewal.awesome_travel.inquiry.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class InquiryAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long inquiryId; // 연결된 문의 ID

    private Long adminId; // 답변한 관리자 ID

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    public static InquiryAnswer create(Long inquiryId, Long adminId, String content) {
        InquiryAnswer answer = new InquiryAnswer();
        answer.inquiryId = inquiryId;
        answer.adminId = adminId;
        answer.content = content;
        answer.createdAt = LocalDateTime.now();
        return answer;
    }
}

