package renewal.awesome_travel.air.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.awesome_travel.air.utiles.SeatClassType;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "SeatClass")
public class SeatClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_class_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "air_id", nullable = false)
    private Air air;

    @Enumerated(EnumType.STRING) // Enum을 DB에 문자열로 저장
    @Column(nullable = false)
    private SeatClassType classType; // 좌석 등급

    @Column(nullable = false)
    private Integer price; // 해당 등급의 가격

    @Column(nullable = false)
    private Integer maxSeats; // 해당 등급의 최대 좌석 수

    @Column(nullable = false)
    private Integer availableSeats; // 잔여 좌석 수

    public SeatClass(SeatClassType classType, Integer price, Integer maxSeats, Integer availableSeats) {
        this.classType = classType;
        this.price = price;
        this.maxSeats = maxSeats;
        this.availableSeats = availableSeats;
    }

    public void updateSeatClass(Integer price, Integer maxSeats, Integer availableSeats) {
        if (price != null) this.price = price;
        if (maxSeats != null) this.maxSeats = maxSeats;
        if (availableSeats != null) this.availableSeats = availableSeats;
    }

    public void decreaseAvailableSeats(int count) {
        if (this.availableSeats < count) {
            throw new IllegalStateException("남은 좌석이 부족합니다.");
        }
        this.availableSeats -= count;
    }

    public void increaseAvailableSeats(int count) {
        this.availableSeats += count;
    }


    public void setAir(Air air) {
        this.air = air;
    }
}
