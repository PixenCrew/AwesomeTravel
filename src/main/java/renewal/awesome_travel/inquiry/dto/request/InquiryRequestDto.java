package renewal.awesome_travel.inquiry.dto.request;

import lombok.Getter;
import renewal.common.entity.Inquiry.InquiryCategory;
import renewal.common.entity.Inquiry.InquiryStage;

@Getter
public class InquiryRequestDto {
    private Long purchaseId;
    private String title;
    private String content;
    private InquiryCategory category;

    private Long productId;
    private InquiryStage stage;
}
