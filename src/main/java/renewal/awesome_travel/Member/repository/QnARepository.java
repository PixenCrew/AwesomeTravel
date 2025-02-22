package renewal.awesome_travel.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import renewal.awesome_travel.member.entity.QnA;

import java.util.List;

public interface QnARepository extends JpaRepository<QnA, Long> {
    List<QnA> findByMemberId(Long memberId);
}
