package renewal.awesome_travel.product.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import renewal.common.entity.Product;
import renewal.common.repository.ProductRepository;
import renewal.common.service.ProductServiceCommon;

/**
 * 매일 1회: (1) 최소인원 미달 출발 취소 (2) 상품별 cutoffDays 기준 패키지 좌석 홀드 미사용분 반납.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PackageSeatHoldReleaseScheduler {

    private final ProductServiceCommon productServiceCommon;
    private final ProductRepository productRepo;

    @Scheduled(cron = "0 0 1 * * ?")
    public void releaseUnusedHolds() {
        LocalDate today = LocalDate.now();
        try {
            productServiceCommon.checkAndCancelDeparturesBelowMinCapacity();
        } catch (Exception e) {
            log.warn("Min-capacity check/cancel failed: {}", e.getMessage(), e);
        }
        List<Product> products = productRepo.findByCutoffDaysIsNotNull();
        for (Product product : products) {
            if (product.getCutoffDays() == null) {
                continue;
            }
            LocalDate departDate = today.plusDays(product.getCutoffDays());
            try {
                productServiceCommon.releaseUnusedSeatsForProductAndDepartDate(product.getId(), departDate);
            } catch (Exception e) {
                log.warn("Package seat hold release failed productId={}, departDate={}: {}", product.getId(), departDate, e.getMessage(), e);
            }
        }
    }
}
