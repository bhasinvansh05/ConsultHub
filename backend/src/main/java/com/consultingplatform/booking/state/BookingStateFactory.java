package com.consultingplatform.booking.state;

/**
 * Factory for creating BookingState instances based on status string
 */
public class BookingStateFactory {
    
    public static BookingState createState(String status) {
        return switch (status) {
            case "REQUESTED" -> new RequestedState();
            case "CONFIRMED" -> new ConfirmedState();
            case "PAID" -> new PaidState();
            case "COMPLETED" -> new CompletedState();
            case "CANCELLED" -> new CancelledState();
            case "REJECTED" -> new RejectedState();
            default -> new RequestedState();
        };
    }
}
