package renewal.awesome_travel.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.awesome_travel.product.entity.Product;
import renewal.awesome_travel.product.entity.ProductList;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "QnA")
public class QnA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "productList_id")
    private ProductList productList;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column()
    private LocalDateTime answer_date;

    @Column(columnDefinition = "TEXT")
    private String answer;

    // 회원이 상품에 대한 질문
    public static QnA createForMemberProduct(Member member, Product product, String content) {
        QnA qna = new QnA();
        qna.member = member;
        qna.product = product;
        qna.content = content;
        qna.date = LocalDateTime.now();
        return qna;
    }

    // 회원이 옵션별 상품에 대한 질문
    public static QnA createForMemberProductList(Member member, ProductList productList, String content) {
        QnA qna = new QnA();
        qna.member = member;
        qna.productList = productList;
        qna.content = content;
        qna.date = LocalDateTime.now();
        return qna;
    }
}
