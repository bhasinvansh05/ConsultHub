package com.consultingplatform.consultant.web;

import com.consultingplatform.consultant.domain.ConsultingService;
import com.consultingplatform.consultant.repository.ConsultingServiceRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
public class ConsultingServiceController {

    private final ConsultingServiceRepository serviceRepository;

    public ConsultingServiceController(ConsultingServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    /**
     * UC1: Browse all active consulting services
     */
    @GetMapping
    public List<ConsultingService> getAllActiveServices(
            @RequestParam(required = false) Long consultantId,
            @RequestParam(required = false) String serviceType) {
        
        if (consultantId != null) {
            return serviceRepository.findByConsultantIdAndIsActiveTrue(consultantId);
        }
        
        if (serviceType != null) {
            return serviceRepository.findByServiceTypeAndIsActiveTrue(serviceType);
        }
        
        return serviceRepository.findByIsActiveTrue();
    }

    /**
     * UC1: Get service details by ID
     */
    @GetMapping("/{id}")
    public ConsultingService getServiceById(@PathVariable Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
    }
}
