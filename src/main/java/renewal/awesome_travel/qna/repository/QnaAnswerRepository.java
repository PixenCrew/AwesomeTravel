package renewal.awesome_travel.qna.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import renewal.common.entity.QnaAnswer;

import java.util.List;

public interface QnaAnswerRepository extends JpaRepository<QnaAnswer, Long> {
    List<QnaAnswer> findByQnaId(Long qnaId);
}
