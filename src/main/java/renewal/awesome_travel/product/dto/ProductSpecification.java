package renewal.awesome_travel.product.dto;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import renewal.common.entity.Product;
import renewal.common.entity.Tour;

public class ProductSpecification {

    public static Specification<Product> keywordContains(String keyword) {
        return (root, query, builder) -> {
            if (query != null) {
                query.distinct(true); // 중복 제거
            }
            // Product.keywords
            Join<Product, String> productKeywords = root.join("keywords", JoinType.LEFT);
            Predicate productPredicate = builder.like(productKeywords, "%" + keyword + "%");

            // Product.Tour.keywords
            Join<Product, Tour> tourJoin = root.join("tour", JoinType.LEFT);
            Join<Tour, String> tourKeywords = tourJoin.join("keywords", JoinType.LEFT);
            Predicate tourPredicate = builder.like(tourKeywords, "%" + keyword + "%");

            return builder.or(productPredicate, tourPredicate);
        };
    }

    // !!!!!!!!!!!!!!!Tour.name LIKE %name%!!!!!!!!!!!!!!!!!!!!
    public static Specification<Tour> nameContains(String name) {
        return (root, query, builder) -> builder.like(root.get("name"), "%" + name + "%");
    }
}
