package renewal.awesome_travel.user.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import renewal.common.entity.User;

@Getter
@AllArgsConstructor
public class UserResponseDto {

    private Long id;
    private String email;
    private String name;
    private String profileImage;
    private String phone;
    private LocalDate birthDate;

    private Long point;
    private String grade;

    private String role;
    private String status;

    // 기타 정보
    private Boolean emailVerified;
    private Boolean marketingConsent;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.profileImage = user.getProfileImage();
        this.phone = user.getPhone();
        this.birthDate = user.getBirthDate();
        this.point = user.getPoint();
        this.grade = user.getGrade().name();

        this.role = user.getRole().name();
        this.status = user.getStatus().name();
    }
}
