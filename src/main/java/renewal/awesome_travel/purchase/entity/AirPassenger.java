package renewal.awesome_travel.purchase.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import renewal.awesome_travel.purchase.dto.requestDto.AirPassengerUpdateRequestDto;
import renewal.awesome_travel.purchase.utiles.Sex;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "AirPassenger")
public class AirPassenger extends BasePassenger {

    @ManyToOne
    @JoinColumn(name = "air_purchase_id", nullable = false)
    private AirPurchase airPurchase;

    //요구사항(기내식, 좌석, 수하물 등등) 추후 enum이나 엔티티로 변경
    @ManyToMany
    @JoinTable(
            name = "air_passenger_special_requests",
            joinColumns = @JoinColumn(name = "air_passenger_id"),
            inverseJoinColumns = @JoinColumn(name = "special_request_id")
    )
    private Set<SpecialRequest> specialRequests = new HashSet<>(); //요구사항

    public AirPassenger(AirPurchase airPurchase, String name, String number, String email, LocalDate birth, Sex sex, Country nationality, String passport_num, String lastName, String firstName, LocalDate expire) {
        super(name, number, email, birth, sex, nationality, passport_num, lastName, firstName, expire);
        this.airPurchase = airPurchase;
    }

    // SpecialRequest 설정
    public void addSpecialRequests(Set<SpecialRequest> specialRequests) {
        if (specialRequests != null) {
            this.specialRequests.addAll(specialRequests);
        }
    }

    public void updateInfo(AirPassengerUpdateRequestDto dto, Country newCountry, Set<SpecialRequest> newRequests) {
        // BasePassenger 필드 처리
        if (dto.getName() != null) this.name = dto.getName();
        if (dto.getNumber() != null) this.number = dto.getNumber();
        if (dto.getEmail() != null) this.email = dto.getEmail();
        if (dto.getBirth() != null) this.birth = dto.getBirth();
        if (dto.getSex() != null) this.sex = dto.getSex();
        if (newCountry != null) this.nationality = newCountry;
        if (dto.getPassportNum() != null) this.passport_num = dto.getPassportNum();
        if (dto.getLastName() != null) this.lastName = dto.getLastName();
        if (dto.getFirstName() != null) this.firstName = dto.getFirstName();
        if (dto.getExpire() != null) this.expire = dto.getExpire();

        // AirPassenger 고유 필드 처리
        if (newRequests != null) {
            this.specialRequests.clear();
            this.specialRequests.addAll(newRequests);
        }
    }


}
