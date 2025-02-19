package renewal.awesome_travel.banner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BannerDto {

    private Long id;

    private Integer display_order;

    private String title;

    private LocalDate start;

    private LocalDate end;

    private boolean active;

    private String file;

    private String url;
}
