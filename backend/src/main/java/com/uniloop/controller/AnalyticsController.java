package com.uniloop.controller;

import com.uniloop.dto.Dtos.*;
import com.uniloop.security.AuthUser;
import com.uniloop.service.AnalyticsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final AnalyticsService analytics;
    public AnalyticsController(AnalyticsService analytics) { this.analytics = analytics; }

    @GetMapping("/public")
    public Analytics platform() { return analytics.platform(); }

    @GetMapping("/me")
    public MyImpact mine(@AuthenticationPrincipal AuthUser me) { return analytics.mine(me.getUser()); }
}
