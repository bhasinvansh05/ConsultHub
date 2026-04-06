package com.consultingplatform.consultingservice.service;

import org.springframework.stereotype.Service;

import com.consultingplatform.admin.domain.PricingStrategyConfig;
import com.consultingplatform.admin.service.SystemPolicyService;
import com.consultingplatform.consultingservice.domain.ConsultingService;
import com.consultingplatform.consultingservice.repository.ConsultingServiceRepository;
import com.consultingplatform.consultingservice.service.pricing.PricingStrategy;
import com.consultingplatform.consultingservice.service.pricing.PricingStrategyFactory;
import com.consultingplatform.consultingservice.web.dto.ConsultingServiceDto;
import com.consultingplatform.admin.service.ConflictException;
import com.consultingplatform.admin.service.ResourceNotFoundException;
import com.consultingplatform.booking.repository.BookingRepository;

import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConsultingServiceServiceImp implements ConsultingServiceService{
    
    private final ConsultingServiceRepository consultingServiceRepository;
    private final SystemPolicyService systemPolicyService;
    private final PricingStrategyFactory pricingStrategyFactory;
    private final BookingRepository bookingRepository;

    public ConsultingServiceServiceImp(ConsultingServiceRepository consultingServiceRepository,
                                       SystemPolicyService systemPolicyService,
                                       PricingStrategyFactory pricingStrategyFactory,
                                       BookingRepository bookingRepository) {
        this.consultingServiceRepository = consultingServiceRepository;
        this.systemPolicyService = systemPolicyService;
        this.pricingStrategyFactory = pricingStrategyFactory;
        this.bookingRepository = bookingRepository;
    }
    
    @Override
    public ConsultingService createService(ConsultingServiceDto serviceDto) {
        ConsultingService service = new ConsultingService();
        
        service.setServiceType(serviceDto.getServiceType());
        service.setTitle(serviceDto.getTitle());
        service.setDescription(serviceDto.getDescription());
        service.setDurationMinutes(serviceDto.getDurationMinutes());
        service.setBasePrice(serviceDto.getBasePrice());
        service.setIsActive(serviceDto.getIsActive() != null ? serviceDto.getIsActive() : Boolean.TRUE);

        return consultingServiceRepository.save(service);
    }
    
    @Override
    public List<ConsultingService> getAllActiveServices(String serviceType) {
        List<ConsultingService> services;
        if (serviceType != null && !serviceType.trim().isEmpty()) {
            services = consultingServiceRepository.findByServiceTypeAndIsActiveTrue(serviceType);
        } else {
            services = consultingServiceRepository.findByIsActiveTrue();
        }
        return services.stream().map(this::applyPricingStrategy).collect(Collectors.toList());
    }

    @Override
    public ConsultingService getServiceById(Long id) {
        return consultingServiceRepository.findById(id)
                .map(this::applyPricingStrategy)
                .orElse(null);
    }

    @Override
    public List<ConsultingService> getAllServicesForAdmin() {
        return consultingServiceRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Override
    public ConsultingService updateService(Long id, ConsultingServiceDto serviceDto) {
        ConsultingService service = consultingServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulting service not found"));

        service.setServiceType(serviceDto.getServiceType());
        service.setTitle(serviceDto.getTitle());
        service.setDescription(serviceDto.getDescription());
        service.setDurationMinutes(serviceDto.getDurationMinutes());
        service.setBasePrice(serviceDto.getBasePrice());
        if (serviceDto.getIsActive() != null) {
            service.setIsActive(serviceDto.getIsActive());
        }

        return consultingServiceRepository.save(service);
    }

    @Override
    public void deleteService(Long id) {
        if (!consultingServiceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Consulting service not found");
        }
        if (bookingRepository.countByServiceId(id) > 0) {
            throw new ConflictException(
                    "Cannot delete this service while bookings reference it. Deactivate it instead, or resolve those bookings first.");
        }
        consultingServiceRepository.deleteById(id);
    }

    private ConsultingService applyPricingStrategy(ConsultingService service) {
        Optional<PricingStrategyConfig> configOpt = systemPolicyService.getPolicyConfig("PRICING_STRATEGY", PricingStrategyConfig.class);
        
        if (configOpt.isPresent()) {
            PricingStrategyConfig config = configOpt.get();
            PricingStrategy strategy = pricingStrategyFactory.getStrategy(config.getStrategyType());
            
            // Set the original price before modifying the base price
            service.setOriginalPrice(service.getBasePrice());
            java.math.BigDecimal newPrice = strategy.calculatePrice(service.getBasePrice(), config);
            
            service.setBasePrice(newPrice);
        }
        return service;
    }
}
