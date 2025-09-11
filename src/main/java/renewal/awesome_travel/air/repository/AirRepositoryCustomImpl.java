package renewal.awesome_travel.air.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import renewal.awesome_travel.air.dto.AirSearchRequestDto;
// import renewal.awesome_travel.air.dto.request.SegmentRequest;
// import renewal.awesome_travel.air.dto.response.AirResponseDto;
// import renewal.awesome_travel.air.dto.response.MultiTripSearchResultDto;
// import renewal.awesome_travel.air.dto.response.RoundTripSearchResultDto;
import renewal.common.entity.Air;
import renewal.common.entity.QAir;
import renewal.common.entity.QSeatClass;
// import renewal.awesome_travel.air.mapper.AirMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AirRepositoryCustomImpl implements AirRepositoryCustom {

    // private final JPAQueryFactory queryFactory;

    // @Override
    // public Page<?> search(AirSearchRequestDto req) {
    //     String tripType = req.getTripType();

    //     if ("MULTI".equalsIgnoreCase(tripType)) {
    //         if (Boolean.TRUE.equals(req.getUseCombinationSearch())) {
    //             return searchMultiFlightCombinations(req);
    //         } else {
    //             return searchMultiSegments(req);
    //         }
    //     } else if ("ROUND_TRIP".equalsIgnoreCase(tripType) && Boolean.TRUE.equals(req.getUseCombinationSearch())) {
    //         return searchRoundTripCombinations(req);
    //     } else {
    //         return searchSingleSegment(req);
    //     }
    // }

    // private Page<AirResponseDto> searchSingleSegment(AirSearchRequestDto req) {
    //     QAir air = QAir.air;
    //     QSeatClass seat = QSeatClass.seatClass;

    //     BooleanBuilder builder = buildCommonConditions(req, air, seat);
    //     builder.and(air.depart.eq(req.getDepart()));
    //     builder.and(air.arrive.eq(req.getArrive()));
    //     builder.and(air.depart_time.eq(req.getDepartDateFrom().toString()));

    //     OrderSpecifier<?> orderSpecifier = resolveSort(req, air, seat);
    //     Pageable pageable = PageRequest.of(req.getPage(), req.getSize());

    //     List<AirResponseDto> content = queryFactory
    //             .selectFrom(seat)
    //             .join(seat.air, air)
    //             .where(builder)
    //             .orderBy(orderSpecifier)
    //             .offset(pageable.getOffset())
    //             .limit(pageable.getPageSize())
    //             .fetch()
    //             .stream()
    //             .map(sc -> AirMapper.toAirResponseDto(sc.getAir(), sc.getClassType()))
    //             .filter(Objects::nonNull)
    //             .collect(Collectors.toList());

    //     long total = queryFactory
    //             .select(seat.count())
    //             .from(seat)
    //             .join(seat.air, air)
    //             .where(builder)
    //             .fetchOne();

    //     return new PageImpl<>(content, pageable, total);
    // }

    // private Page<AirResponseDto> searchMultiSegments(AirSearchRequestDto req) {
    //     QAir air = QAir.air;
    //     QSeatClass seat = QSeatClass.seatClass;

    //     List<AirResponseDto> result = new ArrayList<>();

    //     for (SegmentRequest segment : req.getMultiSegments()) {
    //         BooleanBuilder builder = buildCommonConditions(req, air, seat);
    //         builder.and(air.depart.eq(segment.getDepart()));
    //         builder.and(air.arrive.eq(segment.getArrive()));
    //         builder.and(air.depart_time.eq(segment.getDepartDate().toString()));

    //         OrderSpecifier<?> orderSpecifier = resolveSort(req, air, seat);
    //         Pageable pageable = PageRequest.of(req.getPage(), req.getSize());

    //         List<AirResponseDto> content = queryFactory
    //                 .selectFrom(seat)
    //                 .join(seat.air, air)
    //                 .where(builder)
    //                 .orderBy(orderSpecifier)
    //                 .offset(pageable.getOffset())
    //                 .limit(pageable.getPageSize())
    //                 .fetch()
    //                 .stream()
    //                 .map(sc -> AirMapper.toAirResponseDto(sc.getAir(), sc.getClassType()))
    //                 .filter(Objects::nonNull)
    //                 .collect(Collectors.toList());

    //         result.addAll(content);
    //     }

    //     Pageable pageable = PageRequest.of(req.getPage(), req.getSize());
    //     int start = (int) pageable.getOffset();
    //     int end = Math.min(start + pageable.getPageSize(), result.size());
    //     List<AirResponseDto> paged = result.subList(start, end);

    //     return new PageImpl<>(paged, pageable, result.size());
    // }

    // private Page<RoundTripSearchResultDto> searchRoundTripCombinations(AirSearchRequestDto req) {
    //     QAir air = QAir.air;

    //     List<Air> goFlights = queryFactory.selectFrom(air)
    //             .where(air.depart.eq(req.getDepart())
    //                     .and(air.arrive.eq(req.getArrive()))
    //                     .and(air.depart_time.eq(req.getDepartDateFrom().toString())))
    //             .fetch();

    //     List<Air> returnFlights = queryFactory.selectFrom(air)
    //             .where(air.depart.eq(req.getArrive())
    //                     .and(air.arrive.eq(req.getDepart()))
    //                     .and(air.depart_time.eq(req.getDepartDateTo().toString())))
    //             .fetch();

    //     List<RoundTripSearchResultDto> result = new ArrayList<>();
    //     for (Air go : goFlights) {
    //         for (Air back : returnFlights) {
    //             RoundTripSearchResultDto dto = RoundTripSearchResultDto.of(go, back, req.getSeatClassType());
    //             if (dto != null) result.add(dto);
    //         }
    //     }

    //     result.sort(getRoundTripComparator(req.getSortField(), req.getSortOrder()));

    //     Pageable pageable = PageRequest.of(req.getPage(), req.getSize());
    //     int start = (int) pageable.getOffset();
    //     int end = Math.min(start + pageable.getPageSize(), result.size());
    //     List<RoundTripSearchResultDto> paged = result.subList(start, end);

    //     return new PageImpl<>(paged, pageable, result.size());
    // }

    // private Page<MultiTripSearchResultDto> searchMultiFlightCombinations(AirSearchRequestDto req) {
    //     QAir air = QAir.air;

    //     // 구간별 항공편 리스트 수집
    //     List<List<Air>> segmentFlights = new ArrayList<>();
    //     for (SegmentRequest segment : req.getMultiSegments()) {
    //         BooleanBuilder builder = buildCommonConditions(req, air, QSeatClass.seatClass);
    //         builder.and(air.depart.eq(segment.getDepart()));
    //         builder.and(air.arrive.eq(segment.getArrive()));
    //         builder.and(air.depart_time.eq(segment.getDepartDate().toString()));

    //         List<Air> flights = queryFactory.selectFrom(air).where(builder).fetch();
    //         segmentFlights.add(flights);
    //     }

    //     // 가능한 조합 생성 (카르테시안 곱)
    //     List<List<Air>> combinations = new ArrayList<>();
    //     generateCartesianProduct(segmentFlights, combinations, new ArrayList<>(), 0);

    //     // DTO 변환 및 정렬
    //     List<MultiTripSearchResultDto> result = combinations.stream()
    //             .map(list -> MultiTripSearchResultDto.of(list, req.getSeatClassType()))
    //             .filter(Objects::nonNull)
    //             .sorted(getMultiTripComparator(req.getSortField(), req.getSortOrder())) // ← 여기서 사용됨
    //             .collect(Collectors.toList());

    //     // 페이징
    //     Pageable pageable = PageRequest.of(req.getPage(), req.getSize());
    //     int start = (int) pageable.getOffset();
    //     int end = Math.min(start + pageable.getPageSize(), result.size());
    //     List<MultiTripSearchResultDto> paged = result.subList(start, end);

    //     return new PageImpl<>(paged, pageable, result.size());
    // }

    // private void generateCartesianProduct(List<List<Air>> input, List<List<Air>> result, List<Air> current, int depth) {
    //     if (depth == input.size()) {
    //         result.add(new ArrayList<>(current));
    //         return;
    //     }
    //     for (Air air : input.get(depth)) {
    //         current.add(air);
    //         generateCartesianProduct(input, result, current, depth + 1);
    //         current.remove(current.size() - 1);
    //     }
    // }

    // private BooleanBuilder buildCommonConditions(AirSearchRequestDto req, QAir air, QSeatClass seat) {
    //     BooleanBuilder builder = new BooleanBuilder();

    //     if (req.getDirectOnly() != null && req.getDirectOnly()) {
    //         builder.and(air.stopovers.eq(0));
    //     }
    //     if (req.getSeatClassType() != null) {
    //         builder.and(seat.classType.eq(req.getSeatClassType()));
    //     }
    //     if (req.getAirlines() != null && !req.getAirlines().isEmpty()) {
    //         builder.and(air.airline.in(req.getAirlines()));
    //     }
    //     if (req.getDepartTimeStart() != null && req.getDepartTimeEnd() != null) {
    //         builder.and(air.depart_time.between(
    //                 req.getDepartTimeStart().toString(),
    //                 req.getDepartTimeEnd().toString()
    //         ));
    //     }
    //     if (req.getMinPrice() != null) {
    //         builder.and(seat.price.goe(req.getMinPrice()));
    //     }
    //     if (req.getMaxPrice() != null) {
    //         builder.and(seat.price.loe(req.getMaxPrice()));
    //     }

    //     int totalPassengers = req.getAdultCount() + req.getChildCount();
    //     if (req.isIncludeInfantInSeatCount()) {
    //         totalPassengers += req.getInfantCount();
    //     }
    //     builder.and(seat.availableSeats.goe(totalPassengers));

    //     return builder;
    // }

    // private OrderSpecifier<?> resolveSort(AirSearchRequestDto req, QAir air, QSeatClass seat) {
    //     if ("price".equals(req.getSortField())) {
    //         return "desc".equalsIgnoreCase(req.getSortOrder()) ? seat.price.desc() : seat.price.asc();
    //     } else if ("departTime".equals(req.getSortField())) {
    //         return "desc".equalsIgnoreCase(req.getSortOrder()) ? air.depart_time.desc() : air.depart_time.asc();
    //     } else {
    //         return seat.price.asc();
    //     }
    // }

    // private Comparator<RoundTripSearchResultDto> getRoundTripComparator(String sortField, String sortOrder) {
    //     return switch (sortField) {
    //         case "price" -> "desc".equalsIgnoreCase(sortOrder)
    //                 ? Comparator.comparingInt(RoundTripSearchResultDto::getTotalPrice).reversed()
    //                 : Comparator.comparingInt(RoundTripSearchResultDto::getTotalPrice);
    //         case "go_depart_time" -> "desc".equalsIgnoreCase(sortOrder)
    //                 ? Comparator.comparing(r -> r.getOutbound().getDepartTime(), Comparator.reverseOrder())
    //                 : Comparator.comparing(r -> r.getOutbound().getDepartTime());
    //         case "return_depart_time" -> "desc".equalsIgnoreCase(sortOrder)
    //                 ? Comparator.comparing(r -> r.getInbound().getDepartTime(), Comparator.reverseOrder())
    //                 : Comparator.comparing(r -> r.getInbound().getDepartTime());
    //         default -> Comparator.comparingInt(RoundTripSearchResultDto::getTotalPrice);
    //     };
    // }

    // private Comparator<MultiTripSearchResultDto> getMultiTripComparator(String sortField, String sortOrder) {
    //     return switch (sortField) {
    //         case "price" -> "desc".equalsIgnoreCase(sortOrder)
    //                 ? Comparator.comparingInt(MultiTripSearchResultDto::getTotalPrice).reversed()
    //                 : Comparator.comparingInt(MultiTripSearchResultDto::getTotalPrice);
    //         case "segment1_depart_time" -> "desc".equalsIgnoreCase(sortOrder)
    //                 ? Comparator.comparing(
    //                 dto -> dto.getSegments().get(0).getDepartTime(), Comparator.reverseOrder())
    //                 : Comparator.comparing(dto -> dto.getSegments().get(0).getDepartTime());
    //         case "segment2_depart_time" -> "desc".equalsIgnoreCase(sortOrder)
    //                 ? Comparator.comparing(
    //                 dto -> dto.getSegments().size() > 1 ? dto.getSegments().get(1).getDepartTime() : "9999-99-99",
    //                 Comparator.reverseOrder())
    //                 : Comparator.comparing(
    //                 dto -> dto.getSegments().size() > 1 ? dto.getSegments().get(1).getDepartTime() : "9999-99-99");
    //         default -> Comparator.comparingInt(MultiTripSearchResultDto::getTotalPrice);
    //     };
    // }


}
