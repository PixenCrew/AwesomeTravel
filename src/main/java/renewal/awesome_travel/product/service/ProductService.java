package renewal.awesome_travel.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    // private final ProductRepository productRepository;
    // private final AirRepository airRepository;
    // private final HotelRepository hotelRepository;
    // private final TagRepository tagRepository;

    // public Product createProduct(ProductRequestDto productRequestDto) {
    //     List<Tag> tags = tagRepository.findAllById(productRequestDto.getTagIds());

    //     Product savedProduct = productRepository.save(ProductMapper.toProduct(productRequestDto, tags, null));

    //     List<ProductList> productLists = productRequestDto.getProductLists().stream()
    //             .map(dto -> {
    //                 Air air = airRepository.findById(dto.getAir_id()).orElse(null);
    //                 Hotel hotel = hotelRepository.findById(dto.getHotel_id()).orElse(null);
    //                 return ProductListMapper.toProductList(dto, savedProduct, air, hotel);
    //             })
    //             .collect(Collectors.toList());

    //     savedProduct.getProductLists().addAll(productLists);
    //     return productRepository.save(savedProduct);  // ProductList도 저장됨 (Cascade.ALL 설정 시)
    // }


}
