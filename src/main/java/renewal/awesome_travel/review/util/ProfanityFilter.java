package renewal.awesome_travel.review.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ProfanityFilter {

    private Set<String> bannedWords = new HashSet<>();

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("banned-words.txt");
            if (resource.exists()) {
                try (InputStream inputStream = resource.getInputStream();
                     BufferedReader reader = new BufferedReader(
                             new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty() && !line.startsWith("#")) { // 빈 줄과 주석 제외
                            bannedWords.add(line.toLowerCase()); // 소문자로 변환하여 저장
                        }
                    }
                    log.info("비속어 필터 초기화 완료: {} 개의 단어 로드", bannedWords.size());
                }
            } else {
                log.warn("banned-words.txt 파일을 찾을 수 없습니다.");
            }
        } catch (IOException e) {
            log.error("비속어 필터 초기화 실패", e);
        }
    }

    /**
     * 텍스트에 비속어가 포함되어 있는지 확인
     * @param text 검사할 텍스트
     * @return 비속어가 포함되어 있으면 true
     */
    public boolean containsProfanity(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        String lowerText = text.toLowerCase();
        
        // 공백과 특수문자 제거하여 단어 단위로 검사
        String normalizedText = lowerText.replaceAll("[^가-힣a-z0-9]", "");
        
        // 전체 텍스트에서 비속어 포함 여부 확인
        for (String bannedWord : bannedWords) {
            if (normalizedText.contains(bannedWord)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 텍스트에서 비속어를 찾아 반환
     * @param text 검사할 텍스트
     * @return 발견된 비속어, 없으면 null
     */
    public String findProfanity(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        String lowerText = text.toLowerCase();
        String normalizedText = lowerText.replaceAll("[^가-힣a-z0-9]", "");
        
        for (String bannedWord : bannedWords) {
            if (normalizedText.contains(bannedWord)) {
                return bannedWord;
            }
        }
        
        return null;
    }
}


