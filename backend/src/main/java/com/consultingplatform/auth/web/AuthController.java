package com.consultingplatform.auth.web;

import com.consultingplatform.security.CustomUserDetails;
import com.consultingplatform.security.JwtTokenUtil;
import com.consultingplatform.user.domain.User;
import com.consultingplatform.user.repository.UserRepository;
import com.consultingplatform.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenUtil jwtTokenUtil,
                          UserRepository userRepository,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsernameOrEmail(), req.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
            Object principal = auth.getPrincipal();
            if (!(principal instanceof CustomUserDetails)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Invalid authentication principal");
            }
            CustomUserDetails cud = (CustomUserDetails) principal;
            String token = jwtTokenUtil.generateToken(cud);

            AuthResponse resp = new AuthResponse(token, cud.getId(), cud.getUsername());
            return ResponseEntity.ok(resp);
        } catch (org.springframework.security.authentication.DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Your account is disabled, inactive, or pending approval."));
        } catch (org.springframework.security.core.AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Stateless JWT: logout is a no-op server-side unless a blacklist is implemented.
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        // Check if email already exists
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already in use"));
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("firstName", req.getFirstName());
        payload.put("lastName", req.getLastName());
        payload.put("email", req.getEmail());
        payload.put("password", req.getPassword());
        payload.put("phoneNumber", req.getPhoneNumber());
        String requestedRole = req.getRole();
        String role;
        if ("ADMIN".equalsIgnoreCase(requestedRole)) {
            if (!"EECS3311".equals(req.getAdminCode())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Invalid admin registration code"));
            }
            role = "ADMIN";
        } else if ("CONSULTANT".equalsIgnoreCase(requestedRole)) {
            role = "CONSULTANT";
        } else {
            role = "CLIENT";
        }
        payload.put("role", role);

        User created = userService.createUser(payload);
        // Simple response with created user id and email
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", created.getId(),
                "email", created.getEmail()
        ));
    }
}
