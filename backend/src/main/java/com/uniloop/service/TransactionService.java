package com.uniloop.service;

import com.uniloop.config.ApiException;
import com.uniloop.dto.Dtos.*;
import com.uniloop.model.*;
import com.uniloop.repository.BorrowTransactionRepository;
import com.uniloop.repository.ItemRepository;
import com.uniloop.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * Transaction lifecycle:
 * REQUESTED -> APPROVED (handover QR) -> HANDED_OVER (return QR) -> RETURNED
 *          \-> REJECTED               \-> CANCELLED
 */
@Service
public class TransactionService {
    private static final EnumSet<TransactionStatus> OPEN =
            EnumSet.of(TransactionStatus.REQUESTED, TransactionStatus.APPROVED, TransactionStatus.HANDED_OVER);

    private static final int TRUST_ON_TIME_RETURN = 5;
    private static final int TRUST_LATE_RETURN = -10;
    private static final int TRUST_LENDER_REWARD = 2;

    private final BorrowTransactionRepository txs;
    private final ItemRepository items;
    private final UserRepository users;

    public TransactionService(BorrowTransactionRepository txs, ItemRepository items, UserRepository users) {
        this.txs = txs;
        this.items = items;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> mine(User user) {
        return txs.findForUser(user.getId()).stream().map(t -> TransactionDto.from(t, user.getId())).toList();
    }

    @Transactional
    public TransactionDto request(User borrower, BorrowRequest r) {
        Item item = items.findById(r.itemId()).orElseThrow(() -> ApiException.notFound("Item not found"));
        if (item.getOwner().getId().equals(borrower.getId()))
            throw ApiException.badRequest("You can't borrow your own item");
        if (!item.isAvailable())
            throw ApiException.conflict("This item is not available right now");
        if (borrower.getTrustScore() < 20)
            throw ApiException.forbidden("Your trust score is too low to borrow. Return items on time to rebuild it.");
        if (txs.existsByItemIdAndBorrowerIdAndStatusIn(item.getId(), borrower.getId(), OPEN))
            throw ApiException.conflict("You already have an open request for this item");

        int days = r.days() == null ? Math.min(3, item.getMaxBorrowDays()) : r.days();
        if (days > item.getMaxBorrowDays())
            throw ApiException.badRequest("This item can be borrowed for at most " + item.getMaxBorrowDays() + " days");

        BorrowTransaction t = new BorrowTransaction();
        t.setItem(item);
        t.setBorrower(borrower);
        t.setMessage(r.message());
        t.setBorrowDays(days);
        t.setDueDate(LocalDate.now().plusDays(days));
        return TransactionDto.from(txs.save(t), borrower.getId());
    }

    @Transactional
    public TransactionDto approve(User owner, Long id) {
        BorrowTransaction t = load(id);
        requireOwner(t, owner);
        requireStatus(t, TransactionStatus.REQUESTED);
        Item item = t.getItem();
        if (!item.isAvailable())
            throw ApiException.conflict("Item is already reserved for someone else");
        item.setAvailable(false);                       // reserve it
        t.setStatus(TransactionStatus.APPROVED);
        t.setApprovedAt(Instant.now());
        t.setHandoverToken(newToken());
        return TransactionDto.from(t, owner.getId());
    }

    @Transactional
    public TransactionDto reject(User owner, Long id) {
        BorrowTransaction t = load(id);
        requireOwner(t, owner);
        requireStatus(t, TransactionStatus.REQUESTED);
        t.setStatus(TransactionStatus.REJECTED);
        return TransactionDto.from(t, owner.getId());
    }

    @Transactional
    public TransactionDto cancel(User borrower, Long id) {
        BorrowTransaction t = load(id);
        if (!t.getBorrower().getId().equals(borrower.getId()))
            throw ApiException.forbidden("Only the borrower can cancel this request");
        if (t.getStatus() != TransactionStatus.REQUESTED && t.getStatus() != TransactionStatus.APPROVED)
            throw ApiException.conflict("This request can no longer be cancelled");
        if (t.getStatus() == TransactionStatus.APPROVED) t.getItem().setAvailable(true);  // release reservation
        t.setStatus(TransactionStatus.CANCELLED);
        t.setHandoverToken(null);
        return TransactionDto.from(t, borrower.getId());
    }

    /** Owner scans the borrower's handover QR. */
    @Transactional
    public TransactionDto verifyHandover(User owner, Long id, String token) {
        BorrowTransaction t = load(id);
        requireOwner(t, owner);
        requireStatus(t, TransactionStatus.APPROVED);
        requireToken(t.getHandoverToken(), token);

        t.setStatus(TransactionStatus.HANDED_OVER);
        t.setHandedOverAt(Instant.now());
        t.setDueDate(LocalDate.now().plusDays(t.getBorrowDays()));   // loan period starts at pickup
        t.setHandoverToken(null);
        t.setReturnToken(newToken());
        return TransactionDto.from(t, owner.getId());
    }

    /** Owner scans the borrower's return QR. Completes the share and updates trust scores. */
    @Transactional
    public TransactionDto verifyReturn(User owner, Long id, String token) {
        BorrowTransaction t = load(id);
        requireOwner(t, owner);
        requireStatus(t, TransactionStatus.HANDED_OVER);
        requireToken(t.getReturnToken(), token);

        boolean late = LocalDate.now().isAfter(t.getDueDate());
        t.setStatus(TransactionStatus.RETURNED);
        t.setReturnedAt(Instant.now());
        t.setReturnToken(null);
        t.getItem().setAvailable(true);

        User borrower = t.getBorrower();
        borrower.setTrustScore(borrower.getTrustScore() + (late ? TRUST_LATE_RETURN : TRUST_ON_TIME_RETURN));
        User lender = t.getItem().getOwner();
        lender.setTrustScore(lender.getTrustScore() + TRUST_LENDER_REWARD);
        users.save(borrower);
        users.save(lender);
        return TransactionDto.from(t, owner.getId());
    }

    // ---- helpers ----
    private BorrowTransaction load(Long id) {
        return txs.findById(id).orElseThrow(() -> ApiException.notFound("Transaction not found"));
    }
    private void requireOwner(BorrowTransaction t, User user) {
        if (!t.getItem().getOwner().getId().equals(user.getId()))
            throw ApiException.forbidden("Only the item owner can do this");
    }
    private void requireStatus(BorrowTransaction t, TransactionStatus expected) {
        if (t.getStatus() != expected)
            throw ApiException.conflict("Expected status " + expected + " but it is " + t.getStatus());
    }
    private void requireToken(String expected, String given) {
        boolean ok = expected != null && given != null && MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), given.trim().getBytes(StandardCharsets.UTF_8));
        if (!ok) throw ApiException.badRequest("Invalid QR code for this transaction");
    }
    private String newToken() { return UUID.randomUUID().toString(); }
}
