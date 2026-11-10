package com.uniloop.model;

/** Each category carries a rough CO2 estimate (kg) saved when one existing item is shared instead of buying new. */
public enum Category {
    BOOKS(2.5), CALCULATORS(6.0), LAB_EQUIPMENT(8.0), STATIONERY(1.0),
    ELECTRONICS(12.0), SPORTS(5.0), OTHER(2.0);

    private final double co2SavedKg;
    Category(double co2SavedKg) { this.co2SavedKg = co2SavedKg; }
    public double getCo2SavedKg() { return co2SavedKg; }
}
