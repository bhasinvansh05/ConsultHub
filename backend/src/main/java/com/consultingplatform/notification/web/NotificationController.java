package com.consultingplatform.notification.web;

import com.consultingplatform.notification.domain.Notification;
import com.consultingplatform.notification.service.NotificationService;
import com.consultingplatform.security.CustomUserDetails;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(notificationService.getNotificationsForUser(userId));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(userId)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, Authentication authentication) {
        Long userId = resolveUserId(authentication);
        notificationService.markAsRead(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        if (authentication.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.getId();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
}
