package com.uniloop.service;

import com.uniloop.config.ApiException;
import com.uniloop.dto.Dtos.*;
import com.uniloop.model.User;
import com.uniloop.repository.UserRepository;
import com.uniloop.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public AuthResponse register(RegisterRequest r) {
        if (users.existsByEmailIgnoreCase(r.email()))
            throw ApiException.conflict("An account with this email already exists");
        User u = new User();
        u.setName(r.name().trim());
        u.setEmail(r.email().trim().toLowerCase());
        u.setPasswordHash(encoder.encode(r.password()));
        u.setDepartment(r.department());
        users.save(u);
        return new AuthResponse(jwt.generate(u.getId(), u.getEmail()), UserDto.from(u));
    }

    public AuthResponse login(LoginRequest r) {
        User u = users.findByEmailIgnoreCase(r.email().trim())
                .filter(x -> encoder.matches(r.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return new AuthResponse(jwt.generate(u.getId(), u.getEmail()), UserDto.from(u));
    }
}
