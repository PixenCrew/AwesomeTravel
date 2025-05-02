package renewal.awesome_travel.purchase.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import renewal.awesome_travel.purchase.service.AirPurchaseService;

@RequiredArgsConstructor
@Component
@Slf4j
public class AirPurchaseScheduler {

    private final AirPurchaseService airPurchaseService;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void runCancelJob() {
        airPurchaseService.cancelExpiredHolds();
    }
}
