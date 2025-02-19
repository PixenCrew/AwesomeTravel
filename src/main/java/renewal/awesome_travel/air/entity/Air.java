package renewal.awesome_travel.air.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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

    @Column(nullable = false)
    private String airline;

    @Column(nullable = false)
    private String depart;

    @Column(nullable = false)
    private LocalDateTime depart_time;

    @Column(nullable = false)
    private String arrive;

    @Column(nullable = false)
    private LocalDateTime arrive_time;

    @OneToMany(mappedBy = "air", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeatClass> seatClasses = new ArrayList<>();

    public Air(String code, String airline, String depart, LocalDateTime depart_time, String arrive, LocalDateTime arrive_time) {
        this.code = code;
        this.airline = airline;
        this.depart = depart;
        this.depart_time = depart_time;
        this.arrive = arrive;
        this.arrive_time = arrive_time;
    }

    public void updateAir(String code, String airline, Integer price, String depart, LocalDateTime depart_time, String arrive, LocalDateTime arrive_time, Integer max, Integer rest) {
        if (!code.equals(this.code)) this.code = code;
        if (!airline.equals(this.airline)) this.airline = airline;
        if (!depart.equals(this.depart)) this.depart = depart;
        if (!depart_time.equals(this.depart_time)) this.depart_time = depart_time;
        if (!arrive.equals(this.arrive)) this.arrive = arrive;
        if (!arrive_time.equals(this.arrive_time)) this.arrive_time = arrive_time;
    }

    public void addSeatClass(SeatClass seatClass) {
        this.seatClasses.add(seatClass);
//        seatClass.setAir(this); NM 관계에 생성자에 Air 주입시 필요없음
        //tag와 eventProduct에 경우는 MM관계라서 적는다.
    }

    public void removeSeatClass(SeatClass seatClass) {
        this.seatClasses.remove(seatClass);
        seatClass.setAir(null);
    }
}
