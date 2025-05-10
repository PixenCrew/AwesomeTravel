package renewal.awesome_travel.inquiry.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import renewal.awesome_travel.inquiry.entity.Inquiry;
import renewal.awesome_travel.user.entity.User;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    Page<Inquiry> findByUser(User user, Pageable pageable);

    // 어드민 검색용: 키워드(title, content) 검색 + 답변 여부
    @Query("SELECT i FROM Inquiry i WHERE " +
            "( :keyword IS NULL OR i.title LIKE %:keyword% OR i.content LIKE %:keyword% ) AND " +
            "( :isAnswered IS NULL OR i.isAnswered = :isAnswered )")
    Page<Inquiry> searchAdmin(@Param("keyword") String keyword,
                              @Param("isAnswered") Boolean isAnswered,
                              Pageable pageable);

    // 사용자 본인 검색용: 본인 + 키워드(title, content) 검색 + 답변 여부
    @Query("SELECT i FROM Inquiry i WHERE " +
            "i.user = :user AND " +
            "( :keyword IS NULL OR i.title LIKE %:keyword% OR i.content LIKE %:keyword% ) AND " +
            "( :isAnswered IS NULL OR i.isAnswered = :isAnswered )")
    Page<Inquiry> searchUser(@Param("user") User user,
                             @Param("keyword") String keyword,
                             @Param("isAnswered") Boolean isAnswered,
                             Pageable pageable);
}
