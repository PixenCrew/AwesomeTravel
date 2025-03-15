package renewal.awesome_travel.air.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import renewal.awesome_travel.air.dto.ApiResponse;
import renewal.awesome_travel.air.entity.Air;
import renewal.awesome_travel.air.entity.FlightItem;
import renewal.awesome_travel.air.repository.AirRepository;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

@Service
public class OpenApiService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AirRepository airRepository;

    public void fetchAndSaveData() throws UnsupportedEncodingException {

        String SERVICE_KEY = "LlzmSnhpn+oy0xRuNRMG8B7zoQQFfiaIkqMpw6ZfXw1HGSiYmOUPwwNSq1mayIkaQED3E5GUYYzHGX92CVXYgQ==".trim();
        String encodedKey = URLEncoder.encode(SERVICE_KEY, "UTF-8");
        System.out.println("미리 인코딩된 키: " + encodedKey); // 미리 인코딩된 결과 확인

        URI uri = UriComponentsBuilder
                .fromHttpUrl("https://api.odcloud.kr/api/15003087/v1/uddi:705bfaaa-1fee-4b3c-8e89-cbbf0fd57748")
                .queryParam("page", "1")
                .queryParam("perPage", "5")
                .queryParam("serviceKey", encodedKey)  // 여기서는 trim() 후, RestTemplate이 자동 인코딩하도록 함
                .build(true) // true: 이미 인코딩된 값이라고 가정하면 false로 설정
                .toUri();

        System.out.println("최종 uri : " + uri);

        try {
            ApiResponse apiResponse = restTemplate.getForObject(uri, ApiResponse.class);
            if (apiResponse != null && apiResponse.getData() != null) {
                List<FlightItem> items = apiResponse.getData();
                for (FlightItem item : items) {
                    // DB 저장을 위한 엔티티 변환
                    Air air = new Air(
                            item.getFlightNumber(),
                            item.getAirline(),
                            item.getDepartureAirport(),
                            item.getDepartureTime(),
                            item.getArrivalAirport(),
                            item.getArrivalTime()
                    );

                    airRepository.save(air);
                }
            }
        } catch (Exception e) {
            // 예외 처리: 로그 남기기 등
            e.printStackTrace();
        }
    }
}
