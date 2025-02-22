package renewal.awesome_travel.wait.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import renewal.awesome_travel.member.entity.Member;
import renewal.awesome_travel.wait.entity.Wait;

import java.util.List;

public interface WaitRepository extends JpaRepository<Wait, Long> {

    // 현재 로그인한 사용자의 Wait 목록 조회
    List<Wait> findByMember(Member member);
}
