package renewal.awesome_travel.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.awesome_travel.faq.utiles.CsCategory;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "Inquiry")
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private CsCategory category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String ask;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String response;


}
