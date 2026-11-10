package com.uniloop.controller;

import com.uniloop.dto.Dtos.*;
import com.uniloop.security.AuthUser;
import com.uniloop.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService svc;
    public TransactionController(TransactionService svc) { this.svc = svc; }

    @GetMapping
    public List<TransactionDto> mine(@AuthenticationPrincipal AuthUser me) { return svc.mine(me.getUser()); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDto request(@AuthenticationPrincipal AuthUser me, @Valid @RequestBody BorrowRequest r) {
        return svc.request(me.getUser(), r);
    }

    @PostMapping("/{id}/approve")
    public TransactionDto approve(@AuthenticationPrincipal AuthUser me, @PathVariable Long id) { return svc.approve(me.getUser(), id); }

    @PostMapping("/{id}/reject")
    public TransactionDto reject(@AuthenticationPrincipal AuthUser me, @PathVariable Long id) { return svc.reject(me.getUser(), id); }

    @PostMapping("/{id}/cancel")
    public TransactionDto cancel(@AuthenticationPrincipal AuthUser me, @PathVariable Long id) { return svc.cancel(me.getUser(), id); }

    @PostMapping("/{id}/verify-handover")
    public TransactionDto verifyHandover(@AuthenticationPrincipal AuthUser me, @PathVariable Long id, @Valid @RequestBody VerifyRequest r) {
        return svc.verifyHandover(me.getUser(), id, r.token());
    }

    @PostMapping("/{id}/verify-return")
    public TransactionDto verifyReturn(@AuthenticationPrincipal AuthUser me, @PathVariable Long id, @Valid @RequestBody VerifyRequest r) {
        return svc.verifyReturn(me.getUser(), id, r.token());
    }
}
