package renewal.awesome_travel.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import renewal.awesome_travel.comment.dto.request.CommentReportRequestDto;
import renewal.common.entity.Comment;
import renewal.common.entity.CommentReport;
import renewal.awesome_travel.comment.repository.CommentReportRepository;
import renewal.awesome_travel.comment.repository.CommentRepository;
import renewal.common.entity.User;

@Service
@RequiredArgsConstructor
public class CommentReportService {

    private final CommentRepository commentRepository;
    private final CommentReportRepository commentReportRepository;

    @Transactional
    public void reportComment(Long commentId, User reporter, CommentReportRequestDto dto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        // 중복 신고 확인 (엔티티 기반)
        if (commentReportRepository.existsByReporterAndComment(reporter, comment)) {
            throw new IllegalArgumentException("이미 신고한 댓글입니다.");
        }

        CommentReport report = CommentReport.create(reporter, comment, dto.getReason());
        commentReportRepository.save(report);
    }
}
