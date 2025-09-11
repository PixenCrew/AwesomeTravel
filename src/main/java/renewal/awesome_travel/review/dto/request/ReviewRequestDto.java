package renewal.awesome_travel.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ReviewRequestDto {
    @NotBlank
    private String content;

    @Min(1)
    @Max(5)
    private int rating;
}

