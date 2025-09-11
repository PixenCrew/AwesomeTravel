package renewal.awesome_travel.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import renewal.awesome_travel.review.dto.request.ReviewReportRequestDto;
import renewal.common.entity.Review;
import renewal.common.entity.ReviewReport;
import renewal.awesome_travel.review.repository.ReviewReportRepository;
import renewal.awesome_travel.review.repository.ReviewRepository;
import renewal.common.entity.User;

@Service
@RequiredArgsConstructor
public class ReviewReportService {

    private final ReviewRepository ReviewRepository;
    private final ReviewReportRepository ReviewReportRepository;

    @Transactional
    public void reportReview(Long ReviewId, User reporter, ReviewReportRequestDto dto) {
        Review Review = ReviewRepository.findById(ReviewId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        // 중복 신고 확인 (엔티티 기반)
        if (ReviewReportRepository.existsByReporterAndReview(reporter, Review)) {
            throw new IllegalArgumentException("이미 신고한 댓글입니다.");
        }

        ReviewReport report = ReviewReport.create(reporter, Review, dto.getReason());
        ReviewReportRepository.save(report);
    }
}
