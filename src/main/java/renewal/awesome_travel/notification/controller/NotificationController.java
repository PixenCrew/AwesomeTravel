package renewal.awesome_travel.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import renewal.awesome_travel.config.security.CustomUserDetails;
import renewal.common.entity.Notification;
import renewal.awesome_travel.notification.service.NotificationService;
import renewal.common.entity.User;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 센터 페이지
    @GetMapping("/center")
    public String getNotificationCenter(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        User user = userDetails.getUser();
        Page<Notification> notifications = notificationService.getAllNotifications(user.getId(), pageable);
        
        // 안 읽은 알림 개수
        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(user.getId());
        
        model.addAttribute("notifications", notifications.getContent());
        model.addAttribute("totalCount", notifications.getTotalElements());
        model.addAttribute("unreadCount", unreadNotifications.size());
        
        return "fragments/mypage/notificationCenter";
    }

    // 개별 알림 읽음 처리 (API)
    @PatchMapping("/{id}/read")
    @ResponseBody
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        notificationService.markAsRead(id, userDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }
}

// REST API용 컨트롤러 (기존 기능 유지)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
class NotificationApiController {

    private final NotificationService notificationService;

    // 안 읽은 알림 조회
    @GetMapping
    public ResponseEntity<List<Notification>> getUnreadNotifications(@RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }

    // 알림 모두 읽음 처리
    @PatchMapping("/read")
    public ResponseEntity<Void> markAllAsRead(@RequestParam Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}

