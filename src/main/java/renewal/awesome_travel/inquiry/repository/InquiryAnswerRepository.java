package renewal.awesome_travel.inquiry.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import renewal.awesome_travel.inquiry.entity.InquiryAnswer;

import java.util.Optional;

public interface InquiryAnswerRepository extends JpaRepository<InquiryAnswer, Long> {
    Optional<InquiryAnswer> findByInquiryId(Long inquiryId);
}

