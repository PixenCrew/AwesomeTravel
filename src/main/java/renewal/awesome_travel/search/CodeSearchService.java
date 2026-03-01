package renewal.awesome_travel.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import renewal.awesome_travel.search.dto.CodeSearchResponse;
import renewal.awesome_travel.search.repository.CodeSearchRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodeSearchService {

        private final CodeSearchRepository codeSearchRepo;

        // 메모리 캐시
        private List<CodeSearchResponse> cachedList = new ArrayList<>();

        @PostConstruct
        public void init() {
                log.info(">>> 공항/도시 목록 캐싱 시작...");
                cachedList = codeSearchRepo.loadAllCodeSearchItems();
                log.info(">>> 캐싱 완료. 총 {} 건", cachedList.size());
        }

        public List<CodeSearchResponse> searchFromCache(String keyword) {
                if (keyword == null || keyword.isBlank()) {
                        return Collections.emptyList();
                }

                String query = keyword.toLowerCase().trim();

                return cachedList.stream()
                                .filter(item -> item.getCityKor().toLowerCase().contains(query) ||
                                                item.getCityCode().toLowerCase().contains(query) ||
                                                item.getAirportKor().toLowerCase().contains(query) ||
                                                item.getAirportCode().toLowerCase().contains(query))
                                .limit(20) // 너무 많을 때 앞 50개만 반환 (선택)
                                .toList();
        }
}
