package renewal.awesome_travel.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.search.dto.CodeSearchResponse;
import renewal.awesome_travel.search.repository.CodeSearchRepository;

@Service
@RequiredArgsConstructor
public class CodeSearchService {

        private final CodeSearchRepository codeSearchRepo;

        // 메모리 캐시
        private List<CodeSearchResponse> cachedList = new ArrayList<>();

        @PostConstruct
        public void init() {
                System.out.println(">>> 공항/도시 목록 캐싱 시작...");
                cachedList = codeSearchRepo.loadAllCodeSearchItems();
                System.out.println(">>> 캐싱 완료. 총 " + cachedList.size() + " 건");
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
