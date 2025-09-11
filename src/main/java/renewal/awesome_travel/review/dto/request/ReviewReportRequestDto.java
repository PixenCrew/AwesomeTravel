package renewal.awesome_travel.review.dto.request;

import jakarta.validation.constraints.NotNull;
import renewal.common.entity.ReviewReport.ReportReason;

public class ReviewReportRequestDto {

    @NotNull
    private ReportReason reason;

    public ReportReason getReason() {
        return reason;
    }
}

