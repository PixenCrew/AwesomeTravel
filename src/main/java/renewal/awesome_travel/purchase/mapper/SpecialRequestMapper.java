package renewal.awesome_travel.purchase.mapper;

import renewal.awesome_travel.purchase.dto.SpecialRequestDto;
import renewal.awesome_travel.purchase.entity.SpecialRequest;

public class SpecialRequestMapper {

    public static SpecialRequestDto toDto(SpecialRequest specialRequest) {
        if (specialRequest == null) {
            return null;
        }
        return new SpecialRequestDto(
                specialRequest.getId(),
                specialRequest.getRequestType(),
                specialRequest.getDescription()
        );
    }

    public static SpecialRequest toEntity(SpecialRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return new SpecialRequest(
                dto.getRequestType(),
                dto.getDescription()
        );
    }
}
