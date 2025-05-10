package renewal.awesome_travel.comment.dto.request;

import jakarta.validation.constraints.NotNull;
import renewal.awesome_travel.comment.utiles.ReportReason;

public class CommentReportRequestDto {

    @NotNull
    private ReportReason reason;

    public ReportReason getReason() {
        return reason;
    }
}

