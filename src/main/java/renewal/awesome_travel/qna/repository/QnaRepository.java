package renewal.awesome_travel.qna.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import renewal.common.entity.Qna;

public interface QnaRepository extends JpaRepository<Qna, Long> {
    Page<Qna> findByIsAnswered(boolean isAnswered, Pageable pageable);

    Page<Qna> findByTitleContainingOrContentContaining(String titleKeyword, String contentKeyword, Pageable pageable);
}
