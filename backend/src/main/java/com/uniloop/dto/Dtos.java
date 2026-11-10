package com.uniloop.dto;

import com.uniloop.model.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class Dtos {
    private Dtos() {}

    // ----- auth -----
    public record RegisterRequest(@NotBlank String name,
                                  @Email @NotBlank String email,
                                  @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password,
                                  String department) {}
    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
    public record AuthResponse(String token, UserDto user) {}

    // ----- user -----
    public record UserDto(Long id, String name, String email, String department, Role role, int trustScore,
                          String trustLevel) {
        public static UserDto from(User u) {
            return new UserDto(u.getId(), u.getName(), u.getEmail(), u.getDepartment(), u.getRole(),
                    u.getTrustScore(), level(u.getTrustScore()));
        }
        static String level(int s) {
            if (s >= 85) return "Campus Champion";
            if (s >= 65) return "Trusted";
            if (s >= 40) return "Neutral";
            return "Low";
        }
    }
    /** Public subset shown on item cards (no email). */
    public record OwnerDto(Long id, String name, int trustScore, String trustLevel) {
        public static OwnerDto from(User u) {
            return new OwnerDto(u.getId(), u.getName(), u.getTrustScore(), UserDto.level(u.getTrustScore()));
        }
    }

    // ----- items -----
    public record ItemRequest(@NotBlank String title,
                              String description,
                              @NotNull Category category,
                              @NotNull ItemCondition condition,
                              String imageUrl,
                              @PositiveOrZero Double estimatedValue,
                              @Min(1) @Max(60) Integer maxBorrowDays) {}

    public record ItemDto(Long id, String title, String description, Category category, ItemCondition condition,
                          String imageUrl, double estimatedValue, int maxBorrowDays, boolean available,
                          OwnerDto owner, Instant createdAt) {
        public static ItemDto from(Item i) {
            return new ItemDto(i.getId(), i.getTitle(), i.getDescription(), i.getCategory(), i.getCondition(),
                    i.getImageUrl(), i.getEstimatedValue(), i.getMaxBorrowDays(), i.isAvailable(),
                    OwnerDto.from(i.getOwner()), i.getCreatedAt());
        }
    }

    // ----- transactions -----
    public record BorrowRequest(@NotNull Long itemId, String message, @Min(1) Integer days) {}
    public record VerifyRequest(@NotBlank String token) {}

    /**
     * Tokens are only included for the borrower (who shows them as a QR code).
     * The owner never receives them - the owner can only submit what they scanned.
     */
    public record TransactionDto(Long id, ItemDto item, OwnerDto borrower, TransactionStatus status, String message,
                                 LocalDate dueDate, boolean overdue, String handoverToken, String returnToken,
                                 Instant requestedAt, Instant approvedAt, Instant handedOverAt, Instant returnedAt) {
        public static TransactionDto from(BorrowTransaction t, Long viewerId) {
            boolean isBorrower = t.getBorrower().getId().equals(viewerId);
            boolean overdue = t.getStatus() == TransactionStatus.HANDED_OVER && t.getDueDate() != null
                    && LocalDate.now().isAfter(t.getDueDate());
            String handover = isBorrower && t.getStatus() == TransactionStatus.APPROVED ? t.getHandoverToken() : null;
            String ret = isBorrower && t.getStatus() == TransactionStatus.HANDED_OVER ? t.getReturnToken() : null;
            return new TransactionDto(t.getId(), ItemDto.from(t.getItem()), OwnerDto.from(t.getBorrower()),
                    t.getStatus(), t.getMessage(), t.getDueDate(), overdue, handover, ret,
                    t.getRequestedAt(), t.getApprovedAt(), t.getHandedOverAt(), t.getReturnedAt());
        }
    }

    // ----- analytics -----
    public record MonthlyPoint(String month, long shares) {}
    public record CategoryPoint(String category, long shares) {}
    public record LeaderboardEntry(Long userId, String name, int trustScore, long sharesCompleted, double co2SavedKg) {}
    public record Analytics(long totalItems, long availableItems, long totalUsers, long completedShares,
                            long activeLoans, long overdueLoans, double co2SavedKg, double moneySaved,
                            double treesEquivalent, List<MonthlyPoint> monthly, List<CategoryPoint> byCategory,
                            List<LeaderboardEntry> leaderboard) {}
    public record MyImpact(long itemsLent, long itemsBorrowed, double co2SavedKg, double moneySaved, int trustScore,
                           String trustLevel, Map<String, Long> statusCounts) {}
}
