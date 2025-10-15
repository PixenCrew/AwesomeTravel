package renewal.awesome_travel.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailCheckResponseDto {
    private boolean exists;       // 이메일 존재 여부
    private String provider;      // LOCAL / GOOGLE / NAVER / null
    private String message;       // 사용자 안내 메시지
}

