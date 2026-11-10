package com.uniloop.config;

import com.uniloop.model.*;
import com.uniloop.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Inserts a few demo users, items and finished loans on an empty database so the UI isn't blank. */
@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final ItemRepository items;
    private final BorrowTransactionRepository txs;
    private final PasswordEncoder encoder;
    private final boolean enabled;

    public DataSeeder(UserRepository users, ItemRepository items, BorrowTransactionRepository txs,
                      PasswordEncoder encoder, @Value("${uniloop.seed-demo-data:true}") boolean enabled) {
        this.users = users; this.items = items; this.txs = txs; this.encoder = encoder; this.enabled = enabled;
    }

    @Override
    public void run(String... args) {
        if (!enabled || users.count() > 0) return;

        User aarav = user("Aarav Sharma", "aarav@uniloop.dev", "Computer Science", 82);
        User diya = user("Diya Patel", "diya@uniloop.dev", "Electronics", 74);
        User kabir = user("Kabir Singh", "kabir@uniloop.dev", "Mechanical", 58);

        Item dsa = item(aarav, "Introduction to Algorithms (CLRS)", "3rd edition, light highlighting.", Category.BOOKS, ItemCondition.GOOD, 3200, 14);
        Item calc = item(diya, "Casio fx-991EX Scientific Calculator", "Works perfectly, fresh batteries.", Category.CALCULATORS, ItemCondition.GOOD, 1400, 7);
        Item kit = item(diya, "Arduino Uno Starter Kit", "Board, breadboard, jumper wires and sensors.", Category.ELECTRONICS, ItemCondition.FAIR, 2500, 10);
        item(kabir, "Engineering Drawing Set", "Drafter, compass and set squares.", Category.STATIONERY, ItemCondition.GOOD, 900, 7);
        item(aarav, "Lab Coat (Size M)", "Washed and ready.", Category.LAB_EQUIPMENT, ItemCondition.GOOD, 600, 5);
        item(kabir, "Badminton Racket Pair", "Two rackets and a shuttle tube.", Category.SPORTS, ItemCondition.FAIR, 1800, 3);

        // some completed loans so the dashboard has data
        completed(dsa, diya, 40); completed(dsa, kabir, 22); completed(calc, aarav, 12);
        completed(calc, kabir, 5); completed(kit, aarav, 2);

        System.out.println("\n[UniLoop] Demo data created. Log in with aarav@uniloop.dev / password123 (also diya@, kabir@)\n");
    }

    private User user(String name, String email, String dept, int trust) {
        User u = new User();
        u.setName(name); u.setEmail(email); u.setDepartment(dept); u.setTrustScore(trust);
        u.setPasswordHash(encoder.encode("password123"));
        return users.save(u);
    }

    private Item item(User owner, String title, String desc, Category c, ItemCondition cond, double value, int maxDays) {
        Item i = new Item();
        i.setOwner(owner); i.setTitle(title); i.setDescription(desc); i.setCategory(c); i.setCondition(cond);
        i.setEstimatedValue(value); i.setMaxBorrowDays(maxDays);
        return items.save(i);
    }

    private void completed(Item item, User borrower, int daysAgo) {
        BorrowTransaction t = new BorrowTransaction();
        t.setItem(item); t.setBorrower(borrower); t.setStatus(TransactionStatus.RETURNED);
        Instant returned = Instant.now().minus(daysAgo, ChronoUnit.DAYS);
        t.setHandedOverAt(returned.minus(4, ChronoUnit.DAYS));
        t.setReturnedAt(returned);
        t.setDueDate(LocalDate.now().minusDays(daysAgo - 1L));
        txs.save(t);
    }
}
