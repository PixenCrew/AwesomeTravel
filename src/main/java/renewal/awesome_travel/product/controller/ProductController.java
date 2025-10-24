package renewal.awesome_travel.product.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.air.repository.SeatClassRepository;
import renewal.awesome_travel.product.dto.ProductSearchRequestDto;
import renewal.awesome_travel.product.repository.ProductRepository;
import renewal.awesome_travel.product.service.ProductService;
import renewal.common.entity.Location;
import renewal.common.entity.Location.LocationType;
import renewal.common.entity.Product;
import renewal.common.entity.Product.DepartTimeType;
import renewal.common.entity.Schedule;
import renewal.common.entity.SeatClass;
import renewal.common.entity.Tour;

@Controller
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepo;
    private final SeatClassRepository seatClassRepo;

    @GetMapping
    public String getProductSearch(Model model) {

        model.addAttribute("searchRequest", new ProductSearchRequestDto());


        List<Product> products = productRepo.findAll(); // 전체 상품들
        List<Product> availProducts = new ArrayList<>(); // 항공권 있는 상품들
        LocalDate today = LocalDate.now();

        productLoop: // Product 루프 레이블
        for (Product product : products) {
            
            Hibernate.initialize(product.getTour());

            Tour tour = product.getTour();

            // 초기 가격 계산 (투어 가격 추가부터 시작)
            Long finalPriceAdult = tour.getPriceAdult();
            Long finalPriceYouth = tour.getPriceYouth();
            Long finalPriceInfant = tour.getPriceInfant();

            // Product => Schedules
            List<Schedule> schedules = product.getTour().getSchedules();
            for (Schedule sced : schedules) {

                //Schedules => Locations
                List<Location> locations = sced.getLocations();
                for (Location loc : locations) {
                    LocationType type = loc.getLocationType();
                    if (type == LocationType.AIR) {

                        // 시작일 + 출발시간대 => 출발시간 범위 LocalDateTime ~ LocalDateTime
                        // 오늘기준 가장 빠른 출발일 + Schedule N일차
                        LocalDate departDate = today.plusDays(product.getCutoffDays()).plusDays(sced.getDay());
                        DepartTimeType dtt = product.getDepartTimeType();

                        // 첫날(출발편)인 경우만 오전,오후,새벽 구분함
                        int startHour = sced.getDay() == 0 ? dtt.getStartHour() : 0;
                        int endHour = sced.getDay() == 0 ? dtt.getEndHour() : 23;

                        LocalDateTime startDateTime = departDate.atTime(startHour, 0); // 부터
                        LocalDateTime endDateTime = departDate.atTime(endHour, 59, 59); // 까지
                        System.out.println("Searching SeatClass from " + startDateTime + " to " + endDateTime);

                        // 대상 항공권
                        SeatClass finalSeat = seatClassRepo.findLowestPriceSeat(
                                startDateTime,
                                endDateTime,
                                loc.getDepartAirport(),
                                loc.getArriveAirport(),
                                product.getSeatClassTypes());

                        if (finalSeat == null) {
                            continue productLoop;
                        }
                        loc.setSeatClass(finalSeat);

                        // 항공권 가격 합산
                        finalPriceAdult += finalSeat.getPriceAdult();
                        finalPriceYouth += finalSeat.getPriceYouth();
                        finalPriceInfant += finalSeat.getPriceInfant();

                    } else if (type == LocationType.HOTEL) {

                        // 호텔 가격 합산
                        finalPriceAdult += loc.getHotel().getPrice();
                        finalPriceYouth += loc.getHotel().getPrice();
                        // finalPriceInfant += loc.getHotel().getPrice(); 영유아는 호텔 인원 카운트 안함
                    }
                }
            }

            // 최종 가격 입력
            product.setFinalPriceAdult(finalPriceAdult);
            product.setFinalPriceYouth(finalPriceYouth);
            product.setFinalPriceInfant(finalPriceInfant);

            // 결과 목록에 추가
            availProducts.add(product);
        }
        
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

}
