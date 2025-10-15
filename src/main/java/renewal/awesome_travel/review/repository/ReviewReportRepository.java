package renewal.awesome_travel.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import renewal.common.entity.Review;
import renewal.common.entity.ReviewReport;
import renewal.common.entity.User;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
    boolean existsByReporterAndReview(User reporter, Review comment);

}

