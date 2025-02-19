package renewal.awesome_travel.notice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.awesome_travel.config.AuditingFields;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "Notice")
public class Notice extends AuditingFields {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean fix;
}
