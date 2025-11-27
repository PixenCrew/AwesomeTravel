package renewal.awesome_travel.notice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import renewal.common.entity.Notice;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    
    Page<Notice> findByIsVisibleTrueOrderByFixDescCreatedAtDesc(Pageable pageable);
    
    List<Notice> findByIsVisibleTrueAndFixTrueOrderByPriorityAscCreatedAtDesc();
}

