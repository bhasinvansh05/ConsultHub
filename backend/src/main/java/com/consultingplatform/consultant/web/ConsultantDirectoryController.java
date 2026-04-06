package com.consultingplatform.consultant.web;

import com.consultingplatform.user.domain.Consultant;
import com.consultingplatform.user.repository.ConsultantRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Directory of registered consultants (for clients browsing the platform).
 */
@RestController
@RequestMapping("/api/consultants")
public class ConsultantDirectoryController {

    private final ConsultantRepository consultantRepository;

    public ConsultantDirectoryController(ConsultantRepository consultantRepository) {
        this.consultantRepository = consultantRepository;
    }

    @GetMapping
    public List<Consultant> listConsultants() {
        return consultantRepository.findAll();
    }
}
