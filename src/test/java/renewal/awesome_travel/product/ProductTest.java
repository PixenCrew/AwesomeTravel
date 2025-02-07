package renewal.awesome_travel.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import renewal.awesome_travel.product.entity.Product;
import renewal.awesome_travel.product.entity.ProductList;

import static org.xmlunit.util.Linqy.count;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class ProductTest {

    public Product productCreate() {
        return new Product("hong", "seoul", "japan", 10, 30, "travel");
    }

    @DisplayName("상품생성테스트")
    @Test
    void ProductCreateTest() {
        Product product = new Product("hong", "seoul", "japan", 10, 30, "travel");
        System.out.println("결과값 : " + product.getName() + ", " + product.getDepart() + ", " + product.getDestination() + ", " + product.getDuration() + ", " +product.getMax_reserve() + ", " +product.getDescription());
        product.update("jung", "busan", "america", 7, 20, "fun");
        System.out.println("결과값 : " + product.getName() + ", " + product.getDepart() + ", " + product.getDestination() + ", " + product.getDuration() + ", " +product.getMax_reserve() + ", " +product.getDescription());
    }

    @Test
    @DisplayName("리스트 추가 테스트")
    void ProductListAddTest() {
        Product product = productCreate();
        ProductList productList = new ProductList(product, 123, 456, "2월14일", 150000, 5);
        product.addProductList(productList);
        System.out.println("product count = " + count(product.getProductLists()));
        System.out.println("productList = " + product.getProductLists().get(0).getPrice());
    }
}
