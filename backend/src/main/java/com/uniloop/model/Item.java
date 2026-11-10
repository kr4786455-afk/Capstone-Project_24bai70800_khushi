package com.uniloop.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "items")
public class Item {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category = Category.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_condition", nullable = false)
    private ItemCondition condition = ItemCondition.GOOD;

    private String imageUrl;

    /** What it would cost to buy new (INR) - used for "money saved" analytics. */
    private double estimatedValue;

    /** Max number of days this item can be borrowed. */
    private int maxBorrowDays = 7;

    @Column(nullable = false)
    private boolean available = true;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private User owner;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public ItemCondition getCondition() { return condition; }
    public void setCondition(ItemCondition condition) { this.condition = condition; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public double getEstimatedValue() { return estimatedValue; }
    public void setEstimatedValue(double estimatedValue) { this.estimatedValue = estimatedValue; }
    public int getMaxBorrowDays() { return maxBorrowDays; }
    public void setMaxBorrowDays(int maxBorrowDays) { this.maxBorrowDays = maxBorrowDays; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public Instant getCreatedAt() { return createdAt; }
}
