package renewal.awesome_travel.air.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.awesome_travel.air.utiles.AirStatus;
import renewal.awesome_travel.air.utiles.FlightType;
import renewal.awesome_travel.air.utiles.SeatClassType;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "Air")
public class Air {

    @Id
    @Column(name = "air_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airline_code", nullable = false)
    private Airline airline;

    @Column(nullable = false)
    private String depart;

    @Column(nullable = false)
    private String depart_time;

    @Column(nullable = false)
    private String arrive;

    @Column(nullable = false)
    private String arrive_time;

    @Column(nullable = false)
    private Integer stopovers; // 경유 횟수 (0 = 직항, 1 이상 = 경유)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AirStatus status = AirStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightType flightType;

    @OneToMany(mappedBy = "air", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeatClass> seatClasses = new ArrayList<>();
}
