package renewal.awesome_travel.config.security;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {

        HttpSession session = request.getSession();
        
        // ============================================
        // 특별 케이스 1: 결제 페이지로 리다이렉트 (다음 단계 버튼 클릭 후 로그인)
        // ============================================
        // 예약하기 페이지에서 "다음 단계" 버튼 클릭 → 로그인 → 결제 정보 페이지로 이동
        Boolean redirectToPayment = (Boolean) session.getAttribute("redirectToPayment");
        if (redirectToPayment != null && redirectToPayment) {
            String paymentUrl = (String) session.getAttribute("paymentRedirectUrl");
            session.removeAttribute("redirectToPayment");
            session.removeAttribute("paymentRedirectUrl");
            
            if (paymentUrl != null && !paymentUrl.isEmpty()) {
                // 결제 페이지로 리다이렉트
                response.sendRedirect(paymentUrl);
                return;
            }
        }
        
        // ============================================
        // 특별 케이스 2: 항공편 선택 후 로그인
        // ============================================
        // 항공 검색 결과에서 항공편 선택 → 로그인 → 결제 정보 페이지로 이동
        @SuppressWarnings("unchecked")
        Map<String, Object> airReservationInfo = (Map<String, Object>) session.getAttribute("pendingAirReservation");
        
        if (airReservationInfo != null) {
            // 항공편 예약 정보가 있으면 세션에 저장하고 홈으로 리다이렉트
            // 프론트엔드에서 세션 정보를 확인하여 항공편 상세 페이지로 이동 후 결제 페이지로 이동
            session.removeAttribute("pendingAirReservation"); // 사용 후 제거
            session.setAttribute("redirectToAirReservation", true);
            session.setAttribute("airReservationData", airReservationInfo);
            
            // 홈으로 리다이렉트
            response.sendRedirect("/");
            return;
        }
        
        // ============================================
        // 특별 케이스 2: 일반 상품 예약하기 후 로그인
        // ============================================
        // 상품 상세 페이지에서 예약하기 버튼 클릭 → 로그인 → 예약 모달 자동 오픈
        @SuppressWarnings("unchecked")
        Map<String, Object> reservationInfo = (Map<String, Object>) session.getAttribute("pendingReservation");
        
        if (reservationInfo != null) {
            // 예약 정보가 있으면 세션에 저장하고 홈으로 리다이렉트
            // 프론트엔드에서 세션 정보를 확인하여 예약 모달 열기
            session.removeAttribute("pendingReservation"); // 사용 후 제거
            session.setAttribute("redirectToReservation", true);
            session.setAttribute("reservationData", reservationInfo);
            
            // 홈으로 리다이렉트
            response.sendRedirect("/");
            return;
        }
        
        // ============================================
        // 일반 케이스: 찜, 마이페이지 등에서 로그인
        // ============================================
        // 예약 정보가 없으면 항상 홈으로 리다이렉트
        // (찜, 마이페이지에서 로그인하기 버튼 클릭 시)
        response.sendRedirect("/");
    }
}

