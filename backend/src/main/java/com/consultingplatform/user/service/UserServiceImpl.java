package com.consultingplatform.user.service;

import com.consultingplatform.admin.domain.ConsultantApprovalStatus;
import com.consultingplatform.admin.domain.ConsultantRegistration;
import com.consultingplatform.admin.repository.ConsultantRegistrationRepository;
import com.consultingplatform.user.domain.Admin;
import com.consultingplatform.user.domain.Client;
import com.consultingplatform.user.domain.Consultant;
import com.consultingplatform.user.domain.User;
import com.consultingplatform.user.repository.UserRepository;
import com.consultingplatform.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import com.consultingplatform.security.PasswordService;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ConsultantRegistrationRepository consultantRegistrationRepository;
    private final PasswordService passwordService;
    private final NotificationService notificationService;

    public UserServiceImpl(UserRepository userRepository,
                          ConsultantRegistrationRepository consultantRegistrationRepository,
                          PasswordService passwordService,
                          NotificationService notificationService) {
        this.userRepository = userRepository;
        this.consultantRegistrationRepository = consultantRegistrationRepository;
        this.passwordService = passwordService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public User createUser(Map<String, Object> userData) {
        String role = (String) userData.get("role");
        
        User user;
        switch (role) {
            case "CLIENT" -> user = mapToClient(userData);
            case "CONSULTANT" -> user = mapToConsultant(userData);
            case "ADMIN" -> user = mapToAdmin(userData);
            default -> throw new IllegalArgumentException("Invalid role: " + role);
        }
        
        User savedUser = userRepository.save(user);
        
        // If consultant, create registration entry for admin approval
        if (savedUser instanceof Consultant consultant) {
            ConsultantRegistration registration = new ConsultantRegistration();
            registration.setConsultantId(savedUser.getId());
            registration.setStatus(ConsultantApprovalStatus.PENDING);
            registration.setCreatedAt(Instant.now());
            consultantRegistrationRepository.save(registration);
            notificationService.sendConsultantPendingApprovalNotificationsToAdmins(consultant);
        }
        
        return savedUser;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    @Transactional
    public User updateUser(Long id, Map<String, Object> userData) {
        User user = getUserById(id);
        
        // Update common fields
        if (userData.containsKey("email")) {
            user.setEmail((String) userData.get("email"));
        }
        if (userData.containsKey("firstName")) {
            user.setFirstName((String) userData.get("firstName"));
        }
        if (userData.containsKey("lastName")) {
            user.setLastName((String) userData.get("lastName"));
        }
        if (userData.containsKey("phoneNumber")) {
            user.setPhoneNumber((String) userData.get("phoneNumber"));
        }
        if (userData.containsKey("accountStatus")) {
            user.setAccountStatus((String) userData.get("accountStatus"));
        }
        
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    private Client mapToClient(Map<String, Object> data) {
        Client client = new Client();
        setCommonFields(client, data);
        return client;
    }
    
    private Consultant mapToConsultant(Map<String, Object> data) {
        Consultant consultant = new Consultant();
        setCommonFields(consultant, data);
        // Default consultant to INACTIVE until admin approves
        consultant.setAccountStatus("INACTIVE");
        return consultant;
    }
    
    private Admin mapToAdmin(Map<String, Object> data) {
        Admin admin = new Admin();
        setCommonFields(admin, data);
        if (data.containsKey("permissions")) {
            admin.setPermissions((String) data.get("permissions"));
        }
        return admin;
    }
    
    private void setCommonFields(User user, Map<String, Object> data) {
        user.setEmail((String) data.get("email"));
        // Accept either a plain `password` (hash it via PasswordService) or a pre-hashed `passwordHash`.
        if (data.containsKey("password")) {
            String raw = (String) data.get("password");
            if (raw != null) {
                user.setPasswordHash(passwordService.hash(raw));
            }
        } else if (data.containsKey("passwordHash")) {
            user.setPasswordHash((String) data.get("passwordHash"));
        }
        user.setFirstName((String) data.get("firstName"));
        user.setLastName((String) data.get("lastName"));
        user.setPhoneNumber((String) data.get("phoneNumber"));
        user.setAccountStatus("ACTIVE");
    }
}
