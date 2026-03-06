package com.consultingplatform.booking.web;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for creating a booking.
 * Client only provides their ID and the slot ID - all other details come from the slot.
 */
@Data
public class BookingRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "Slot ID is required")
    private Long slotId;
}
