package renewal.awesome_travel.coupon.mapper;

import renewal.awesome_travel.coupon.dto.CouponDto;
import renewal.awesome_travel.coupon.entity.Coupon;

public class CouponMapper {

    public static CouponDto toDto(Coupon coupon) {
        if (coupon == null) {
            return null;
        }

        return new CouponDto(
                coupon.getId(),
                coupon.getCoupon_id(),
                coupon.getName(),
                coupon.getDescription(),
                coupon.getCouponType(),
                coupon.getValue(),
                coupon.getMax_discount(),
                coupon.getMin_price(),
                coupon.getTarget(),
                coupon.getCurrent(),
                coupon.getIssue_start(),
                coupon.getIssue_end(),
                coupon.getValidFrom(),
                coupon.getValidUntil());
    }

    public static Coupon toEntity(CouponDto couponDto) {
        if (couponDto == null) {
            return null;
        }

        return new Coupon(
                couponDto.getCoupon_id(),
                couponDto.getName(),
                couponDto.getDescription(),
                couponDto.getCouponType(),
                couponDto.getValue(),
                couponDto.getMax_discount(),
                couponDto.getMin_price(),
                couponDto.getTarget(),
                couponDto.getCurrent(),
                couponDto.getIssue_start(),
                couponDto.getIssue_end(),
                couponDto.getValidFrom(),
                couponDto.getValidUntil());
    }
}
