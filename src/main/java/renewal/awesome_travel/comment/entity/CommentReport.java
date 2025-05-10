package renewal.awesome_travel.comment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.awesome_travel.comment.utiles.ReportReason;
import renewal.awesome_travel.member.entity.User;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "comment_id"})
})
public class CommentReport {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    private LocalDateTime reportedAt;

    public static CommentReport create(User reporter, Comment comment, ReportReason reason) {
        CommentReport report = new CommentReport();
        report.reporter = reporter;
        report.comment = comment;
        report.reason = reason;
        report.reportedAt = LocalDateTime.now();
        return report;
    }
}

