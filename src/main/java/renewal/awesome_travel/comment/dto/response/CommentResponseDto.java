package renewal.awesome_travel.comment.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class CommentResponseDto {
    private Long id;
    private String writerName;
    private String content;
    private int rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

