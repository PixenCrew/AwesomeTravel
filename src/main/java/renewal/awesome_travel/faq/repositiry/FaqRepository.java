package renewal.awesome_travel.faq.repositiry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import renewal.common.entity.Faq;
import renewal.common.entity.Faq.FaqCategory;

public interface FaqRepository extends JpaRepository<Faq, Long> {
    Page<Faq> findByCategory(FaqCategory category, Pageable pageable);

}
