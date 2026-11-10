package com.uniloop.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "borrow_transactions")
public class BorrowTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Item item;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private User borrower;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.REQUESTED;

    private String message;

    private LocalDate dueDate;

    /** Requested loan length; the due date is (re)computed from this at pickup. */
    private int borrowDays = 3;

    /** Secret token encoded in the QR the borrower shows to the owner at pickup. */
    private String handoverToken;

    /** Secret token encoded in the QR the borrower shows to the owner when returning. */
    private String returnToken;

    @Column(nullable = false)
    private Instant requestedAt = Instant.now();
    private Instant approvedAt;
    private Instant handedOverAt;
    private Instant returnedAt;

    public Long getId() { return id; }
    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }
    public User getBorrower() { return borrower; }
    public void setBorrower(User borrower) { this.borrower = borrower; }
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public int getBorrowDays() { return borrowDays; }
    public void setBorrowDays(int borrowDays) { this.borrowDays = borrowDays; }
    public String getHandoverToken() { return handoverToken; }
    public void setHandoverToken(String handoverToken) { this.handoverToken = handoverToken; }
    public String getReturnToken() { return returnToken; }
    public void setReturnToken(String returnToken) { this.returnToken = returnToken; }
    public Instant getRequestedAt() { return requestedAt; }
    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
    public Instant getHandedOverAt() { return handedOverAt; }
    public void setHandedOverAt(Instant handedOverAt) { this.handedOverAt = handedOverAt; }
    public Instant getReturnedAt() { return returnedAt; }
    public void setReturnedAt(Instant returnedAt) { this.returnedAt = returnedAt; }
}
