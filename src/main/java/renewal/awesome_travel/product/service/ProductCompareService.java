package renewal.awesome_travel.product.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import renewal.awesome_travel.product.dto.ProductCompareViewDto;
import renewal.common.entity.AirportCode;
import renewal.common.entity.Hotel;
import renewal.common.entity.Location;
import renewal.common.entity.Product;
import renewal.common.entity.Schedule;
import renewal.common.entity.Tour;
import renewal.common.repository.ProductRepository;
import renewal.common.service.ProductServiceCommon;

@Service
@RequiredArgsConstructor
public class ProductCompareService {

    private static final DateTimeFormatter COMPARE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final ProductRepository productRepo;
    private final ProductServiceCommon productServiceCommon;

    public List<ProductCompareViewDto> buildCompareList(List<Long> ids, LocalDate departDate) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<Product> products = productRepo.findAllById(ids);
        Map<Long, ProductCompareViewDto> compareMap = new HashMap<>();

        for (Product product : products) {
            ProductCompareViewDto dto = buildCompareView(product, departDate);
            if (dto != null) {
                compareMap.put(dto.getProductId(), dto);
            }
        }

        return ids.stream()
                .map(compareMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private ProductCompareViewDto buildCompareView(Product product, LocalDate requestedDepartDate) {
        if (product == null) {
            return null;
        }

        Product workingCopy = cloneProduct(product);
        Tour tour = workingCopy.getTour();
        initializeTourRelations(tour);

        LocalDate baseDepartDate = resolveDepartDate(workingCopy, tour, requestedDepartDate);
        Product enriched = enrichProductForCompare(workingCopy, baseDepartDate);

        Long priceValue = enriched.getFinalPriceAdult() != null ? enriched.getFinalPriceAdult() : enriched.getPrice();
        LocalDate departDate = resolveFinalDepartDate(enriched, baseDepartDate);
        LocalDate returnDate = resolveReturnDate(enriched, tour, departDate);

        return ProductCompareViewDto.builder()
                .productId(enriched.getId())
                .title(enriched.getTitle())
                .priceLabel(formatPrice(priceValue))
                .departDateLabel(formatDate(departDate))
                .returnDateLabel(formatDate(returnDate))
                .departLabel(resolveAirportLabel(tour, true))
                .arriveLabel(resolveAirportLabel(tour, false))
                .airlineLabel(resolveAirline(enriched, tour))
                .hotelLabel(resolveHotelLabel(tour))
                .tripLabel(buildTripLabel(tour))
                .build();
    }

    private Product cloneProduct(Product product) {
        try {
            return (Product) product.clone();
        } catch (CloneNotSupportedException e) {
            return product;
        }
    }

    private void initializeTourRelations(Tour tour) {
        if (tour == null || tour.getSchedules() == null) {
            return;
        }
        tour.getSchedules().forEach(schedule -> {
            if (schedule != null && schedule.getLocations() != null) {
                schedule.getLocations().size();
            }
        });
        if (tour.getCountry() != null) {
            tour.getCountry().getCountryKor();
        }
    }

    private Product enrichProductForCompare(Product product, LocalDate departDate) {
        if (product == null || departDate == null) {
            return product;
        }
        Product calculated = productServiceCommon.calcSingleProduct(product, departDate);
        if (calculated != null) {
            return calculated;
        }
        if (product.getFinalPriceAdult() == null) {
            product.setFinalPriceAdult(product.getPrice());
        }
        return product;
    }

    private LocalDate resolveDepartDate(Product product, Tour tour, LocalDate requestedDate) {
        if (requestedDate != null) {
            return requestedDate;
        }
        if (product != null && product.getDepartDateTime() != null) {
            return product.getDepartDateTime().toLocalDate();
        }
        if (tour != null && tour.getStartDate() != null) {
            return tour.getStartDate();
        }
        if (product != null && product.getCutoffDays() != null) {
            return LocalDate.now().plusDays(product.getCutoffDays());
        }
        return LocalDate.now().plusDays(7);
    }

    private LocalDate resolveFinalDepartDate(Product product, LocalDate fallback) {
        if (product != null && product.getDepartDateTime() != null) {
            return product.getDepartDateTime().toLocalDate();
        }
        return fallback;
    }

    private LocalDate resolveReturnDate(Product product, Tour tour, LocalDate departDate) {
        if (product != null && product.getReturnDateTime() != null) {
            return product.getReturnDateTime().toLocalDate();
        }
        long itineraryDays = resolveItineraryDays(tour);
        if (departDate != null && itineraryDays > 0) {
            return departDate.plusDays(Math.max(1, itineraryDays - 1));
        }
        return departDate != null ? departDate.plusDays(1) : null;
    }

    private long resolveItineraryDays(Tour tour) {
        if (tour == null || tour.getSchedules() == null || tour.getSchedules().isEmpty()) {
            return 0L;
        }
        return tour.getSchedules().stream()
                .filter(Objects::nonNull)
                .map(Schedule::getDay)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L) + 1;
    }

    private String resolveAirportLabel(Tour tour, boolean departure) {
        if (tour == null || tour.getSchedules() == null) {
            return "확인 중";
        }

        if (departure) {
            // 출발지: 첫 번째 AIR Location의 출발 공항
            for (Schedule schedule : tour.getSchedules()) {
                if (schedule == null || schedule.getLocations() == null) {
                    continue;
                }
                for (Location location : schedule.getLocations()) {
                    if (location == null || location.getLocationType() != Location.LocationType.AIR) {
                        continue;
                    }
                    if (location.getDepartAirport() != null) {
                        return formatAirport(location.getDepartAirport());
                    }
                }
            }
        } else {
            // 도착지: 첫 번째 Schedule의 모든 AIR Location을 순회하며 마지막 arriveAirport 사용
            if (tour.getSchedules().isEmpty()) {
                return "확인 중";
            }
            
            Schedule firstSchedule = tour.getSchedules().get(0);
            if (firstSchedule == null || firstSchedule.getLocations() == null) {
                return "확인 중";
            }
            
            Location lastArrive = null;
            for (Location location : firstSchedule.getLocations()) {
                if (location == null || location.getLocationType() != Location.LocationType.AIR) {
                    continue;
                }
                if (location.getArriveAirport() != null) {
                    lastArrive = location;
                }
            }
            
            if (lastArrive != null && lastArrive.getArriveAirport() != null) {
                return formatAirport(lastArrive.getArriveAirport());
            }
        }

        return "확인 중";
    }

    private String formatAirport(AirportCode airport) {
        if (airport == null) {
            return "확인 중";
        }
        String name = airport.getAirportKor() != null ? airport.getAirportKor() : airport.getAirportEng();
        return (name != null ? name : "공항") + " (" + airport.getAirportCode() + ")";
    }

    private String resolveAirline(Product product, Tour tour) {
        if (product != null && product.getAirline() != null && product.getAirline().getNameKor() != null) {
            return product.getAirline().getNameKor();
        }
        
        // product에 항공사 정보가 없으면 첫 번째 AIR Location의 seatClass에서 찾기
        if (tour != null && tour.getSchedules() != null) {
            for (Schedule schedule : tour.getSchedules()) {
                if (schedule == null || schedule.getLocations() == null) {
                    continue;
                }
                for (Location location : schedule.getLocations()) {
                    if (location == null || location.getLocationType() != Location.LocationType.AIR) {
                        continue;
                    }
                    if (location.getSeatClass() != null 
                            && location.getSeatClass().getAir() != null
                            && location.getSeatClass().getAir().getAirline() != null
                            && location.getSeatClass().getAir().getAirline().getNameKor() != null) {
                        return location.getSeatClass().getAir().getAirline().getNameKor();
                    }
                }
            }
        }
        
        if (tour != null && tour.getCompany() != null) {
            return tour.getCompany() + " 지정";
        }
        return "확인 중";
    }

    private String resolveHotelLabel(Tour tour) {
        if (tour == null || tour.getSchedules() == null) {
            return "추후 안내";
        }
        
        // 첫 번째 Schedule부터 순회하며 첫 번째 호텔명 찾기
        for (Schedule schedule : tour.getSchedules()) {
            if (schedule == null || schedule.getLocations() == null) {
                continue;
            }
            for (Location location : schedule.getLocations()) {
                if (location == null || location.getLocationType() != Location.LocationType.HOTEL) {
                    continue;
                }
                if (location.getHotel() != null && location.getHotel().getName() != null) {
                    return location.getHotel().getName();
                }
            }
        }
        
        return "추후 안내";
    }

    private String buildTripLabel(Tour tour) {
        long itineraryDays = resolveItineraryDays(tour);
        if (itineraryDays <= 0) {
            return "일정 준비 중";
        }
        long nights = Math.max(0, itineraryDays - 1);
        if (nights > 0) {
            return nights + "박 " + itineraryDays + "일";
        }
        return itineraryDays + "일 일정";
    }

    private String formatPrice(Long price) {
        if (price == null || price <= 0) {
            return "가격 확인 중";
        }
        return String.format("%,d원", price);
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "미정";
        }
        return date.format(COMPARE_DATE_FORMATTER);
    }
}

