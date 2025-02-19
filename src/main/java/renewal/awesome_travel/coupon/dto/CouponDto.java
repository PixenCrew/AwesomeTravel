package renewal.awesome_travel.coupon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import renewal.awesome_travel.coupon.utiles.Coupon_type;
import renewal.awesome_travel.coupon.utiles.Grade;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CouponDto {

    private Long id;

    private Integer coupon_id;

    private String name;

    private String description;

    private Coupon_type couponType;

    private Integer value;

    private Integer max_discount;

    private Integer min_price;

    private Grade target;

    private Integer current;

    private LocalDateTime issue_start;

    private LocalDateTime issue_end;

    private LocalDate expiration;
}
