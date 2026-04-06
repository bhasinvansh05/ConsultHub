package com.consultingplatform.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    public PasswordService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String hash(String rawPassword) {
        if (rawPassword == null) return null;
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) return false;
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
