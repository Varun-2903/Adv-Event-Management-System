package com.eventmanagement.service;

import com.eventmanagement.dto.AuthDtos;
import com.eventmanagement.entity.Role;
import com.eventmanagement.entity.User;
import com.eventmanagement.repository.UserRepository;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder,
                       AuthenticationManager authManager, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwt = jwt;
    }

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest req) {
        String email = normalizeEmail(req.email());
        if (users.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        Role role = Role.PARTICIPANT;
        if (req.role() != null) {
            try { role = Role.valueOf(req.role().toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }
        if (role == Role.ADMIN) role = Role.PARTICIPANT;

        User user = new User();
        user.setName(req.name());
        user.setEmail(email);
        user.setPassword(encoder.encode(req.password()));
        user.setRole(role);
        users.save(user);

        return response(user);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest req) {
        String email = normalizeEmail(req.email());
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, req.password()));

        User user = users.findByEmailIgnoreCase(email).orElseThrow();
        return response(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private AuthDtos.AuthResponse response(User user) {
        String token = jwt.generateToken(new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(),
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().name()))));
        return new AuthDtos.AuthResponse(token, user.getId(), user.getName(),
                user.getEmail(), user.getRole().name());
    }
}
