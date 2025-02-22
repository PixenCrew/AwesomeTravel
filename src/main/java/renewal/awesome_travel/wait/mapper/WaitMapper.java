package renewal.awesome_travel.wait.mapper;

import renewal.awesome_travel.member.entity.Member;
import renewal.awesome_travel.member.mapper.MemberMapper;
import renewal.awesome_travel.product.entity.ProductList;
import renewal.awesome_travel.product.mapper.ProductListMapper;
import renewal.awesome_travel.wait.dto.request.WaitRequestDto;
import renewal.awesome_travel.wait.dto.response.WaitResponseDto;
import renewal.awesome_travel.wait.entity.Wait;

public class WaitMapper {

    public static Wait toEntity(WaitRequestDto waitRequestDto, ProductList productList, Member member) {
        if (waitRequestDto == null) {
            return null;
        }

        return new Wait(
                productList,
                member,
                waitRequestDto.getWait_date()
        );
    }

    public static WaitResponseDto toDto(Wait wait) {
        if (wait == null) {
            return null;
        }

        return new WaitResponseDto(
                wait.getId(),
                ProductListMapper.toProductListDto(wait.getProductList()),
                MemberMapper.memberToMemberDto(wait.getMember()),
                wait.getWait_date(),
                wait.isPurchased()
        );
    }
}
