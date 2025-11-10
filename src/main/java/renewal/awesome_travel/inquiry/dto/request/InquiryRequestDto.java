package renewal.awesome_travel.inquiry.dto.request;

import lombok.Getter;
import renewal.common.entity.Inquiry.InquiryCategory;

@Getter
public class InquiryRequestDto {
    private Long purchaseId;
    private String title;
    private String content;
    private InquiryCategory category;
}
