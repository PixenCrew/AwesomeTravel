package renewal.awesome_travel.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import renewal.awesome_travel.comment.entity.Comment;
import renewal.awesome_travel.comment.entity.CommentReport;
import renewal.awesome_travel.member.entity.User;

public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {
    boolean existsByReporterAndComment(User reporter, Comment comment);

}

