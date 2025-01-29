package renewal.awesome_travel.Member;

import jakarta.persistence.*;
import lombok.*;
// import renewal.awesome_travel.dto.TestDto;

@Entity
@Table(name = "member")
@Getter
@Setter
public class Member {
    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column
    private String name;

    @Column
    private String number;
}
