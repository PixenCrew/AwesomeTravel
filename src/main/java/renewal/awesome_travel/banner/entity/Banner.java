package renewal.awesome_travel.banner.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "Banner")
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer display_order;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDate start;

    @Column(nullable = false)
    private LocalDate end;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, length = 512)
    private String file;

    @Column(nullable = false, length = 512)
    private String url;

}
