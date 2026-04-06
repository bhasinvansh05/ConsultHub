package com.consultingplatform.notification.service;

import com.consultingplatform.admin.domain.NotificationSettingsConfig;
import com.consultingplatform.admin.service.SystemPolicyService;
import com.consultingplatform.booking.domain.Booking;
import com.consultingplatform.consultingservice.domain.ConsultingService;
import com.consultingplatform.consultingservice.repository.ConsultingServiceRepository;
import com.consultingplatform.notification.domain.Notification;
import com.consultingplatform.notification.domain.NotificationType;
import com.consultingplatform.notification.repository.NotificationRepository;
import com.consultingplatform.user.domain.Consultant;
import com.consultingplatform.user.domain.User;
import com.consultingplatform.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ConsultingServiceRepository consultingServiceRepository;
    private final UserRepository userRepository;
    private final SystemPolicyService systemPolicyService;

    private boolean isNotificationSystemEnabled() {
        return systemPolicyService.getPolicyConfig("NOTIFICATION_SETTINGS", NotificationSettingsConfig.class)
                .map(NotificationSettingsConfig::isEnabled)
                .orElse(true);
    }

    public void sendBookingCancelledNotifications(Booking booking) {
        if (!isNotificationSystemEnabled()) {
            return;
        }
        Long clientId = booking.getClientId();
        Long consultantId = booking.getConsultantId();
        if (clientId == null || consultantId == null) {
            return;
        }

        String clientPayload = buildBookingCancelledPayloadForClient(booking);
        String consultantPayload = buildBookingCancelledPayloadForConsultant(booking);

        notificationRepository.save(Notification.builder()
                .userId(clientId)
                .notificationType(NotificationType.BOOKING_CANCELLED)
                .payload(clientPayload)
                .build());

        if (!clientId.equals(consultantId)) {
            notificationRepository.save(Notification.builder()
                    .userId(consultantId)
                    .notificationType(NotificationType.BOOKING_CANCELLED)
                    .payload(consultantPayload)
                    .build());
        }
    }

    /**
     * Client-only: booking was paid, then cancelled — explain refund outcome per policy percentage.
     */
    public void sendPaidBookingCancelledRefundNotificationToClient(Booking booking, double refundPercentage) {
        if (!isNotificationSystemEnabled()) {
            return;
        }

        String payload = buildPaidCancellationRefundPayload(booking, refundPercentage);
        Notification notification = Notification.builder()
                .userId(booking.getClientId())
                .notificationType(NotificationType.PAYMENT_REFUNDED)
                .payload(payload)
                .build();
        notificationRepository.save(notification);
    }

    public void sendBookingAcceptedNotificationToClient(Booking booking) {
        if (!isNotificationSystemEnabled()) {
            return;
        }

        String payload = buildBookingAcceptedPayload(booking);
        Notification clientNotification = Notification.builder()
                .userId(booking.getClientId())
                .notificationType(NotificationType.BOOKING_CONFIRMED)
                .payload(payload)
                .build();
        notificationRepository.save(clientNotification);
    }

    public void sendBookingRejectedNotificationToClient(Booking booking, String reason) {
        if (!isNotificationSystemEnabled()) {
            return;
        }

        String payload = buildBookingRejectedPayload(booking, reason);

        Notification clientNotification = Notification.builder()
                .userId(booking.getClientId())
                .notificationType(NotificationType.BOOKING_REJECTED)
                .payload(payload)
                .build();

        notificationRepository.save(clientNotification);
    }

    public void sendPaymentSuccessNotification(Booking booking) {
        if (!isNotificationSystemEnabled()) {
            return;
        }

        String serviceName = getServiceName(booking);
        String clientName = getUserDisplayName(booking.getClientId(), "the client");

        Notification clientNotification = Notification.builder()
                .userId(booking.getClientId())
                .notificationType(NotificationType.PAYMENT_SUCCESS)
                .payload("Payment successful for " + serviceName + ". Your session is confirmed.")
                .build();

        Notification consultantNotification = Notification.builder()
                .userId(booking.getConsultantId())
                .notificationType(NotificationType.PAYMENT_SUCCESS)
                .payload("Payment received from " + clientName + " for " + serviceName + ". The session is now fully paid.")
                .build();

        notificationRepository.save(clientNotification);
        notificationRepository.save(consultantNotification);
    }

    public void sendConsultantPendingApprovalNotificationsToAdmins(Consultant consultant) {
        if (!isNotificationSystemEnabled()) {
            return;
        }

        String payload = buildConsultantPendingApprovalPayload(consultant);
        for (Long adminId : userRepository.findAllAdminIds()) {
            Notification notification = Notification.builder()
                    .userId(adminId)
                    .notificationType(NotificationType.CONSULTANT_PENDING_APPROVAL)
                    .payload(payload)
                    .build();
            notificationRepository.save(notification);
        }
    }

    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getUserId().equals(userId)) {
                notification.setIsRead(true);
                notificationRepository.save(notification);
            }
        });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllReadByUserId(userId);
    }

    private String buildBookingCancelledPayloadForClient(Booking booking) {
        String serviceName = getServiceName(booking);
        return "Your booking for " + serviceName + " was cancelled. The consultant has been notified.";
    }

    private String buildBookingCancelledPayloadForConsultant(Booking booking) {
        String serviceName = getServiceName(booking);
        String clientLabel = getUserDisplayName(booking.getClientId(), "the client");
        return "The booking for " + serviceName + " with " + clientLabel + " was cancelled. The client has been notified.";
    }

    private String buildPaidCancellationRefundPayload(Booking booking, double refundPercentage) {
        String serviceName = getServiceName(booking);
        if (refundPercentage > 0) {
            int pct = (int) Math.round(refundPercentage);
            return serviceName + " was cancelled after payment. A refund of approximately " + pct
                    + "% of your payment is being processed according to our refund policy.";
        }
        if (booking.getRequestedStartAt() != null) {
            return serviceName + " was cancelled after payment. Per our refund policy, no refund applies for this cancellation timing.";
        }
        return serviceName + " was cancelled after payment. Refund eligibility follows our refund policy; check your payment history for status.";
    }

    private String buildBookingAcceptedPayload(Booking booking) {
        String serviceName = getServiceName(booking);
        return serviceName + " was accepted by the consultant. Complete payment to confirm your session.";
    }

    private String buildBookingRejectedPayload(Booking booking, String reason) {
        String serviceName = getServiceName(booking);
        if (reason != null && !reason.isBlank()) {
            return serviceName + " was declined by the consultant. Reason: " + reason;
        }
        return serviceName + " was declined by the consultant.";
    }

    private String getServiceName(Booking booking) {
        return consultingServiceRepository.findById(booking.getServiceId())
                .map(ConsultingService::getTitle)
                .filter(title -> title != null && !title.isBlank())
                .orElse("your session");
    }

    private String getUserDisplayName(Long userId, String fallback) {
        return userRepository.findById(userId)
                .map(User::getFullName)
                .filter(name -> name != null && !name.isBlank())
                .or(() -> userRepository.findById(userId)
                        .map(User::getEmail)
                        .filter(email -> email != null && !email.isBlank()))
                .orElse(fallback);
    }

    private String buildConsultantPendingApprovalPayload(Consultant consultant) {
        String name = consultant.getFullName() != null ? consultant.getFullName() : consultant.getEmail();
        return "New consultant " + name + " (ID #" + consultant.getId() + ") is registered and pending approval.";
    }
}
