package renewal.awesome_travel.product.mapper;

import org.springframework.stereotype.Component;
import renewal.awesome_travel.product.dto.TagDto;
import renewal.awesome_travel.product.entity.Tag;

@Component
public class TagMapper {

    public static Tag toTag(TagDto tagDto) {
        if (tagDto == null) {
            return null;
        }

        return new Tag(tagDto.getTag());
    }

    public static TagDto toTagDto(Tag tag) {
        if (tag == null) {
            return null;
        }

        return new TagDto(tag.getId(), tag.getTag());
    }
}
