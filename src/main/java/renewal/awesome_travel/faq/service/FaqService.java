package renewal.awesome_travel.faq.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import renewal.awesome_travel.faq.dto.request.FaqRequestDto;
import renewal.awesome_travel.faq.dto.response.FaqResponseDto;
import renewal.awesome_travel.faq.entity.Faq;
import renewal.awesome_travel.faq.repositiry.FaqRepository;
import renewal.awesome_travel.faq.utils.FaqCategory;

@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;

    public Page<FaqResponseDto> getAllFaqs(Pageable pageable) {
        return faqRepository.findAll(pageable)
                .map(this::toDto);
    }

    public Page<FaqResponseDto> getFaqsByCategory(FaqCategory category, Pageable pageable) {
        return faqRepository.findByCategory(category, pageable)
                .map(this::toDto);
    }

    private FaqResponseDto toDto(Faq faq) {
        return FaqResponseDto.builder()
                .id(faq.getId())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .category(faq.getCategory())
                .createdAt(faq.getCreatedAt())
                .build();
    }
}

