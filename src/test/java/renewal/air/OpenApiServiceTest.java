package renewal.air;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import renewal.awesome_travel.AwesomeTravelApplication;
import renewal.awesome_travel.air.repository.AirRepository;
import renewal.awesome_travel.air.service.OpenApiService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(classes = AwesomeTravelApplication.class)
@ActiveProfiles("test")
public class OpenApiServiceTest {

    @Autowired
    private OpenApiService openApiService;

    @Autowired
    private AirRepository airRepository;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    public void setup() {
        // MockRestServiceServer를 RestTemplate에 연결하여 외부 API 호출을 모킹함
        mockServer = MockRestServiceServer.createServer(restTemplate);
        // 테스트 시작 전에 기존 데이터를 초기화
        airRepository.deleteAll();
    }

    @Test
    public void testFetchAndSaveData() throws Exception {
        // open API에서 반환될 JSON 응답 예시 (ApiResponse 구조에 맞게 구성)
        String jsonResponse = "{\n" +
                "  \"page\": 1,\n" +
                "  \"perPage\": 10,\n" +
                "  \"totalCount\": 1,\n" +
                "  \"currentCount\": 1,\n" +
                "  \"matchCount\": 1,\n" +
                "  \"data\": [\n" +
                "    {\n" +
                "      \"항공사\": \"Test Airline\",\n" +
                "      \"운항편명\": \"TA123\",\n" +
                "      \"출발공항\": \"Test Dep Airport\",\n" +
                "      \"도착공항\": \"Test Arr Airport\",\n" +
                "      \"출발시간\": \"10:00\",\n" +
                "      \"도착시간\": \"12:00\",\n" +
                "      \"운항요일\": \"월화수목금\",\n" +
                "      \"시작일자\": \"2023-01-01\",\n" +
                "      \"종료일자\": \"2023-12-31\",\n" +
                "      \"국내_국제\": \"국내\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        // openApiService에서 호출할 URL과 매칭하여 모킹 응답 설정
        String expectedUrl = "https://api.odcloud.kr/api/15003087/v1/uddi:705bfaaa-1fee-4b3c-8e89-cbbf0fd57748?page=1&perPage=10&serviceKey=LlzmSnhpn%2Boy0xRuNRMG8B7zoQQFfiaIkqMpw6ZfXw1HGSiYmOUPwwNSq1mayIkaQED3E5GUYYzHGX92CVXYgQ%3D%3D";
        mockServer.expect(requestTo(expectedUrl))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // 서비스 메서드를 호출하여 데이터를 가져오고 DB에 저장
        openApiService.fetchAndSaveData();

        // DB에 저장된 데이터 건수가 1건인지 검증
        long count = airRepository.count();
        assertEquals(1, count, "데이터가 1건 저장되어야 합니다.");

        // 저장된 데이터를 조회하여 상세 값 확인
        // 예를 들어, 저장된 엔티티의 항공사와 운항편명을 출력합니다.
        airRepository.findAll().forEach(entity -> {
            System.out.println("저장된 데이터:");
            System.out.println("항공사: " + entity.getAirline());
            System.out.println("운항편명: " + entity.getFlightNumber());
            // 추가 필드도 필요에 따라 출력
        });

        // 모킹 서버 호출이 올바르게 수행되었는지 검증
        mockServer.verify();
    }
}
