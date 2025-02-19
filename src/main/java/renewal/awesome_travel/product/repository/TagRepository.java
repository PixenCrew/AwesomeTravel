package renewal.awesome_travel.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import renewal.awesome_travel.product.entity.Tag;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findAllByIdIn(List<Long> tagIds);
}
