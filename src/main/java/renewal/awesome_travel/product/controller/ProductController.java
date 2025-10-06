package renewal.awesome_travel.product.controller;

import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.product.dto.ProductSearchRequestDto;
import renewal.awesome_travel.product.service.ProductService;
import renewal.common.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String getAirSearch(Model model) {
        model.addAttribute("searchRequest", new ProductSearchRequestDto());
        return "product/productSearch";
    }

    @PostMapping("/search")
    public String postAirSearch(@ModelAttribute ProductSearchRequestDto searchRequest, Model model) {
        Sort sort = Sort.by("id").ascending();
        Pageable pageable = PageRequest.of(searchRequest.getPage(), 50, sort);

        Page<Product> result = null;
        if (searchRequest.getKeyword()!=null) {
            result = productService.searchProducts(searchRequest,pageable);
        }

        model.addAttribute("searchResult", result);
        return "product/productResult";
    }

}
