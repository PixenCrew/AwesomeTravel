package renewal.awesome_travel.faq.repositiry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import renewal.common.entity.Faq;
import renewal.common.entity.Faq.FaqCategory;

public interface FaqRepository extends JpaRepository<Faq, Long> {
    Page<Faq> findByCategory(FaqCategory category, Pageable pageable);
    Page<Faq> findByVisibleTrue(Pageable pageable);
    Page<Faq> findByCategoryAndVisibleTrue(FaqCategory category, Pageable pageable);
    
    @Query("SELECT f FROM Faq f WHERE f.visible = true AND (f.question LIKE %:keyword% OR f.answer LIKE %:keyword%)")
    Page<Faq> findByKeywordAndVisibleTrue(@Param("keyword") String keyword, Pageable pageable);
}
