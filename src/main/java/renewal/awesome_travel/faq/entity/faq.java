package renewal.awesome_travel.faq.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.awesome_travel.config.AuditingFields;
import renewal.awesome_travel.faq.utiles.CsCategory;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "faq")
public class faq extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private CsCategory category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String ask;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String response;
}
