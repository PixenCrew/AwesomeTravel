package renewal.air;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import renewal.awesome_travel.AwesomeTravelApplication;
import renewal.awesome_travel.air.repository.AirRepository;
import renewal.awesome_travel.air.service.OpenApiService;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = AwesomeTravelApplication.class)
@ActiveProfiles("test")
public class OpenApiServiceLiveTest {
    @Autowired
    private OpenApiService openApiService;

    @Autowired
    private AirRepository airRepository;

    @BeforeEach
    public void setup() {
        // 테스트 시작 전에 기존 데이터를 초기화
        airRepository.deleteAll();
    }

    @Test
    public void testFetchAndSaveDataLive() throws Exception {
        // 실제 Open API를 호출하여 데이터를 가져오고 DB에 저장
        openApiService.fetchAndSaveData();

        // DB에 저장된 데이터 건수가 0건보다 많으면 실제 데이터가 저장되었다고 볼 수 있음
        long count = airRepository.count();
        System.out.println("저장된 데이터 건수: " + count);
        airRepository.findAll().forEach(entity -> {
            System.out.println("==== 저장된 항공편 데이터 ====");
            System.out.println("항공사: " + entity.getAirline());
            System.out.println("운항편명: " + entity.getCode());
            // 필요에 따라 다른 필드도 출력
        });

        // 적어도 한 건 이상의 데이터가 저장되었음을 검증
        assertTrue(count > 0, "실제 Open API 데이터가 저장되어야 합니다.");
    }
}
