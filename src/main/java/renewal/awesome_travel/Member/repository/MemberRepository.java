package renewal.awesome_travel.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import renewal.awesome_travel.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
    
}
