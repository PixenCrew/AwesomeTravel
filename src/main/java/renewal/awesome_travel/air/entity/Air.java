package renewal.awesome_travel.air.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private String airline;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private String depart;

    @Column(nullable = false)
    private LocalDateTime depart_time;

    @Column(nullable = false)
    private String arrive;

    @Column(nullable = false)
    private LocalDateTime arrive_time;

    @Column(nullable = false)
    private Integer max;

    @Column(nullable = false)
    private Integer rest;

    public Air(String code, String airline, Integer price, String depart, LocalDateTime depart_time, String arrive, LocalDateTime arrive_time, Integer max, Integer rest) {
        this.code = code;
        this.airline = airline;
        this.price = price;
        this.depart = depart;
        this.depart_time = depart_time;
        this.arrive = arrive;
        this.arrive_time = arrive_time;
        this.max = max;
        this.rest = rest;
    }

    public void updateAir(String code, String airline, Integer price, String depart, LocalDateTime depart_time, String arrive, LocalDateTime arrive_time, Integer max, Integer rest) {
        if (!code.equals(this.code)) this.code = code;
        if (!airline.equals(this.airline)) this.airline = airline;
        if (!price.equals(this.price)) this.price = price;
        if (!depart.equals(this.depart)) this.depart = depart;
        if (!depart_time.equals(this.depart_time)) this.depart_time = depart_time;
        if (!arrive.equals(this.arrive)) this.arrive = arrive;
        if (!arrive_time.equals(this.arrive_time)) this.arrive_time = arrive_time;
        if (!max.equals(this.max)) this.max = max;
        if (!rest.equals(this.rest)) this.rest = rest;
    }
}
