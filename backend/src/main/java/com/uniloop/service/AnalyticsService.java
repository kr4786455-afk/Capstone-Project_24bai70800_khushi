package com.uniloop.service;

import com.uniloop.dto.Dtos.*;
import com.uniloop.model.*;
import com.uniloop.repository.BorrowTransactionRepository;
import com.uniloop.repository.ItemRepository;
import com.uniloop.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {
    /** Roughly how much CO2 one mature tree absorbs per year. */
    private static final double KG_CO2_PER_TREE_YEAR = 21.0;

    private final BorrowTransactionRepository txs;
    private final ItemRepository items;
    private final UserRepository users;

    public AnalyticsService(BorrowTransactionRepository txs, ItemRepository items, UserRepository users) {
        this.txs = txs;
        this.items = items;
        this.users = users;
    }

    public Analytics platform() {
        List<BorrowTransaction> done = txs.findByStatus(TransactionStatus.RETURNED);
        List<BorrowTransaction> active = txs.findByStatus(TransactionStatus.HANDED_OVER);

        double co2 = done.stream().mapToDouble(t -> t.getItem().getCategory().getCo2SavedKg()).sum();
        double money = done.stream().mapToDouble(t -> t.getItem().getEstimatedValue()).sum();
        long overdue = active.stream().filter(t -> t.getDueDate() != null && LocalDate.now().isAfter(t.getDueDate())).count();

        // last 6 months (including empty ones)
        YearMonth now = YearMonth.now(ZoneOffset.UTC);
        Map<YearMonth, Long> byMonth = done.stream().collect(Collectors.groupingBy(
                t -> YearMonth.from(t.getReturnedAt().atZone(ZoneOffset.UTC)), Collectors.counting()));
        List<MonthlyPoint> monthly = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth m = now.minusMonths(i);
            monthly.add(new MonthlyPoint(m.getMonth().name().substring(0, 3) + " " + m.getYear() % 100, byMonth.getOrDefault(m, 0L)));
        }

        Map<Category, Long> byCat = done.stream().collect(Collectors.groupingBy(t -> t.getItem().getCategory(), Collectors.counting()));
        List<CategoryPoint> categories = byCat.entrySet().stream()
                .sorted(Map.Entry.<Category, Long>comparingByValue().reversed())
                .map(e -> new CategoryPoint(e.getKey().name(), e.getValue())).toList();

        // leaderboard: users ranked by completed lends
        Map<User, List<BorrowTransaction>> byLender = done.stream().collect(Collectors.groupingBy(t -> t.getItem().getOwner()));
        List<LeaderboardEntry> board = byLender.entrySet().stream()
                .map(e -> new LeaderboardEntry(e.getKey().getId(), e.getKey().getName(), e.getKey().getTrustScore(),
                        e.getValue().size(),
                        e.getValue().stream().mapToDouble(t -> t.getItem().getCategory().getCo2SavedKg()).sum()))
                .sorted(Comparator.comparingLong(LeaderboardEntry::sharesCompleted).reversed()
                        .thenComparing(Comparator.comparingInt(LeaderboardEntry::trustScore).reversed()))
                .limit(5).toList();

        long totalItems = items.count();
        long available = items.search("", true).size();
        return new Analytics(totalItems, available, users.count(), done.size(), active.size(), overdue,
                round(co2), round(money), round(co2 / KG_CO2_PER_TREE_YEAR), monthly, categories, board);
    }

    public MyImpact mine(User user) {
        List<BorrowTransaction> all = txs.findForUser(user.getId());
        List<BorrowTransaction> lent = all.stream()
                .filter(t -> t.getStatus() == TransactionStatus.RETURNED && t.getItem().getOwner().getId().equals(user.getId())).toList();
        List<BorrowTransaction> borrowed = all.stream()
                .filter(t -> t.getStatus() == TransactionStatus.RETURNED && t.getBorrower().getId().equals(user.getId())).toList();
        double co2 = lent.stream().mapToDouble(t -> t.getItem().getCategory().getCo2SavedKg()).sum()
                + borrowed.stream().mapToDouble(t -> t.getItem().getCategory().getCo2SavedKg()).sum();
        double money = borrowed.stream().mapToDouble(t -> t.getItem().getEstimatedValue()).sum();
        Map<String, Long> counts = all.stream().collect(Collectors.groupingBy(t -> t.getStatus().name(), TreeMap::new, Collectors.counting()));
        return new MyImpact(lent.size(), borrowed.size(), round(co2), round(money), user.getTrustScore(),
                UserDto.from(user).trustLevel(), counts);
    }

    private static double round(double v) { return Math.round(v * 10.0) / 10.0; }
}
