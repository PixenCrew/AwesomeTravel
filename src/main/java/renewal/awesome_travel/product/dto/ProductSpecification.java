package renewal.awesome_travel.product.dto;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import renewal.common.entity.Product;
import renewal.common.entity.Tour;

public class ProductSpecification {

    // public static Specification<Product> keywordContains(String keyword) {
    // return (root, query, builder) -> {
    // query.distinct(true);

    // // Product.keywords
    // Join<Product, String> productKeywords = root.join("keywords", JoinType.LEFT);

    // Predicate productPredicate = builder.like(productKeywords.as(String.class),
    // "%" + keyword + "%");

    // // Tour.keywords
    // Join<Product, Tour> tourJoin = root.join("tour", JoinType.LEFT);
    // Join<Tour, String> tourKeywords = tourJoin.join("keywords", JoinType.LEFT);

    // Predicate tourPredicate = builder.like(tourKeywords.as(String.class), "%" +
    // keyword + "%");

    // return builder.or(productPredicate, tourPredicate);
    // };
    // }

    public static Specification<Product> keywordContains(String keyword) {
        return (root, query, builder) -> {
            query.distinct(true);

            Join<Product, String> productKeywords = root.join("keywords", JoinType.LEFT);

            return builder.like(
                    builder.lower(productKeywords.as(String.class)),
                    "%" + keyword.toLowerCase() + "%");
        };
    }

    // !!!!!!!!!!!!!!!Tour.name LIKE %name%!!!!!!!!!!!!!!!!!!!!
    public static Specification<Tour> nameContains(String name) {
        return (root, query, builder) -> builder.like(root.get("name"), "%" + name + "%");
    }
}
