package com.uniloop.controller;

import com.uniloop.dto.Dtos.*;
import com.uniloop.security.AuthUser;
import com.uniloop.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/auth/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest r) { return auth.register(r); }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) { return auth.login(r); }

    @GetMapping("/users/me")
    public UserDto me(@AuthenticationPrincipal AuthUser me) { return UserDto.from(me.getUser()); }
}
