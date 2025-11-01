package renewal.awesome_travel.product.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.product.dto.ProductCalanderDto;
import renewal.awesome_travel.product.dto.ProductSearchRequestDto;
import renewal.awesome_travel.product.repository.ProductRepository;
import renewal.awesome_travel.product.service.ProductService;
import renewal.common.entity.Product;

@Controller
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepo;

    @GetMapping
    public String getProductSearch(Model model) {

        model.addAttribute("searchRequest", new ProductSearchRequestDto());

        List<Product> products = productRepo.findAll(); // 전체 상품들
        LocalDate today = LocalDate.now();

        List<Product> availProducts = productService.calcProduct(products, today);

        model.addAttribute("products", availProducts);

        return "product/productSearch";
    }

    @PostMapping("/search")
    public String postProductSearch(@ModelAttribute ProductSearchRequestDto searchRequest, Model model) {
        Sort sort = Sort.by("id").ascending();
        Pageable pageable = PageRequest.of(searchRequest.getPage(), 50, sort);

        Page<Product> result = null;
        if (searchRequest.getKeyword() != null) {
            result = productService.searchProducts(searchRequest, pageable);
        }

        model.addAttribute("searchResult", result);
        return "product/productResult";
    }

    @GetMapping("/{id}")
    public String getProduct(@PathVariable Long id, Model model) {

        Product target = productRepo.findById(id).get();

        List<ProductCalanderDto> result = new ArrayList<>();

        // 특정 상품에 대해 6개월간 calc해서 return
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 180; i++) {
            LocalDate targetDate = today.plusDays(i);
            // System.out.println("!!!!!!!!!!targetDate = " + targetDate);
            Product calcProduct = productService.calcSingleProduct(target, targetDate);
            if (calcProduct != null) {
                ProductCalanderDto productDto = new ProductCalanderDto(calcProduct);
                result.add(productDto);
                calcProduct.setDepartDateTime(null); // 한 Product에 대해 출발일 필드 초기화
                System.out.println("!!!!!!!!!!productDto.getDepartDateTime() = " + productDto.getDepartDateTime());
                System.out.println("!!!!!!!!!!productDto.getReturnDateTime() = " + productDto.getReturnDateTime());
            }

        }

        model.addAttribute("products", result);

        return "fragments/product/productCalander";
    }

}
