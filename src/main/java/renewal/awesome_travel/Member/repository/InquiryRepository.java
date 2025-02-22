package renewal.awesome_travel.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import renewal.awesome_travel.member.entity.Inquiry;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findByMemberId(Long memberId);
}
