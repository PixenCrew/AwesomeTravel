package renewal.awesome_travel.review.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class ReviewResponseDto {
    private Long id;
    private String writerName;
    private String content;
    private int rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

