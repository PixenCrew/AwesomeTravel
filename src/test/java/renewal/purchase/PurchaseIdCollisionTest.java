package renewal.purchase;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import renewal.awesome_travel.AwesomeTravelApplication;
import renewal.awesome_travel.purchase.repository.PurchaseAirRepository;
import renewal.awesome_travel.purchase.repository.PurchaseProductRepository;
import renewal.common.entity.PurchaseAir;
import renewal.common.entity.PurchaseBase;
import renewal.common.entity.PurchaseProduct;

@SpringBootTest(classes = AwesomeTravelApplication.class)
@Transactional
class PurchaseIdCollisionTest {

    private static final Logger log = LoggerFactory.getLogger(PurchaseIdCollisionTest.class);

    @Autowired
    PurchaseProductRepository purchaseProductRepo;

    @Autowired
    PurchaseAirRepository purchaseAirRepo;

    @Test
    void idCollisionStressTest() {

        int testCount = 1000;

        Set<Long> productIds = new HashSet<>();
        Set<Long> airIds = new HashSet<>();
        Set<Long> crossIds = new HashSet<>();

        // ---------- 1) PurchaseProduct 1000회 저장 ----------
        for (int i = 0; i < testCount; i++) {

            PurchaseProduct product = new PurchaseProduct();
            product.setTitle("상품 테스트" + i);
            product.setPurchaseStatus(PurchaseBase.PurchaseStatus.HOLDING);
            product.setPrice(1000L);
            product.setAdultCount(1L);
            product.setYouthCount(0L);
            product.setInfantCount(0L);
            product.setName("tester");
            product.setNumber("01011112222");
            product.setEmail("test@test.com");
            product.setPurchaseDate(LocalDateTime.now());

            purchaseProductRepo.save(product);

            Long id = product.getId();

            if (productIds.contains(id)) {
                log.error("❌ Product ID 중복 발견: {}", id);
            }

            productIds.add(id);
        }

        // ---------- 2) PurchaseAir 1000회 저장 ----------
        for (int i = 0; i < testCount; i++) {

            PurchaseAir air = new PurchaseAir();
            air.setTitle("항공 테스트" + i);
            air.setPurchaseStatus(PurchaseBase.PurchaseStatus.HOLDING);
            air.setPrice(2000L);
            air.setAdultCount(1L);
            air.setYouthCount(0L);
            air.setInfantCount(0L);
            air.setName("tester");
            air.setNumber("01011112222");
            air.setEmail("test@test.com");
            air.setPurchaseDate(LocalDateTime.now());

            purchaseAirRepo.save(air);

            Long id = air.getId();

            if (airIds.contains(id)) {
                log.error("❌ Air ID 중복 발견: {}", id);
            }

            airIds.add(id);

            // Product ↔ Air 교차 중복 검사
            if (productIds.contains(id)) {
                crossIds.add(id);
                log.error("❌ Product/Air 간 ID 충돌 발생! 중복 ID = {}", id);
            }
        }

        // ---------- 3) 최종 검사 ----------
        log.info("총 Product ID 개수 = {}", productIds.size());
        log.info("총 Air ID 개수 = {}", airIds.size());
        log.info("총 교차 중복 ID 개수 = {}", crossIds.size());

        Assertions.assertEquals(testCount, productIds.size(), "Product ID 중복 발생!");
        Assertions.assertEquals(testCount, airIds.size(), "Air ID 중복 발생!");
        Assertions.assertEquals(0, crossIds.size(), "Product/Air 간 ID 충돌 발생!");
    }
}
