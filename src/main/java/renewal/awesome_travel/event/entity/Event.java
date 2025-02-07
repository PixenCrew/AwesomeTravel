package renewal.awesome_travel.event.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.awesome_travel.event.utils.EventType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "Event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String file;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventProduct> eventProducts = new ArrayList<>();

    public Event(String name, EventType type, LocalDate startDate, LocalDate endDate, String file, String content) {
        this.name = name;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.file = file;
        this.content = content;
    }

    public void addEventProduct(EventProduct eventProduct) {
        if (this.eventProducts == null) {
            this.eventProducts = new ArrayList<>();
        }
        eventProduct.setEvent(this); // 관계 설정 추가
        this.eventProducts.add(eventProduct);
    }

    public void removeEventProduct(EventProduct eventProduct) {
        if (this.eventProducts == null) {
            this.eventProducts = new ArrayList<>();
        }
        eventProduct.setEvent(null); // 관계 설정 추가
    }
}
