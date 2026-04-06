package com.consultingplatform.consultingservice.web;

import com.consultingplatform.consultingservice.domain.ConsultingService;
import com.consultingplatform.consultingservice.service.ConsultingServiceService;
import com.consultingplatform.consultant.service.ConsultantService;
import com.consultingplatform.consultant.web.dto.AvailabilitySlotResponse;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ConsultingServiceController {

    private final ConsultingServiceService consultingServiceService;
    private final ConsultantService consultantService;

    public ConsultingServiceController(ConsultingServiceService consultingServiceService,
                                       ConsultantService consultantService) {
        this.consultingServiceService = consultingServiceService;
        this.consultantService = consultantService;
    }

    /**
     * UC1: Browse all active consulting services
     */
    @GetMapping
    public List<ConsultingService> getAllActiveServices(
            @RequestParam(required = false) String serviceType) {
        
        return consultingServiceService.getAllActiveServices(serviceType);
    }

    /**
     * UC1: Get service details by ID
     */
    @GetMapping("/{id}")
    public ConsultingService getServiceById(@PathVariable Long id) {
        ConsultingService service = consultingServiceService.getServiceById(id);
        if (service == null) {
            throw new RuntimeException("Service not found");
        }
        return service;
    }

    /**
     * UC2: Get availability slots for a specific service
     */
    @GetMapping("/{serviceId}/availability")
    public ResponseEntity<List<AvailabilitySlotResponse>> getAvailabilitySlotsByServiceId(@PathVariable Long serviceId) {
        // Ensure service exists
        if (consultingServiceService.getServiceById(serviceId) == null) {
            throw new RuntimeException("Service not found");
        }

        List<AvailabilitySlotResponse> slots = consultantService.getAvailabilitySlotsByServiceId(serviceId);
        return ResponseEntity.ok(slots);
    }
}
