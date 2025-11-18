package renewal.awesome_travel.product.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import renewal.common.entity.Passenger.AgeGroup;
import renewal.common.entity.Passenger.Sex;

@Getter
@Setter
public class ReservationRequestDto {
    private Long productId;
    private LocalDate departDate; // yyyy-MM-dd 형식
    private String bookerName;
    private String bookerBirth;
    private String bookerGender;
    private String bookerPhone;
    private String bookerEmail;
    private List<PassengerDto> passengers;

    @Getter
    @Setter
    public static class PassengerDto {
        private String lastNameKor;
        private String firstNameKor;
        private LocalDate birth;
        private Sex gender;
        private String phone;
        private String email;
        private AgeGroup ageGroup;
    }
}