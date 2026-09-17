package com.campus.model.enums;

/**
 * Types of campus resources.
 * Unit 2 — Enum with a human-readable display label (demonstrates enum methods).
 */
public enum ResourceType {
    LAB("Computer / Science Lab"),
    CLASSROOM("Classroom"),
    SEMINAR_HALL("Seminar Hall"),
    SPORTS_FACILITY("Sports Facility"),
    EQUIPMENT("Equipment / AV");

    private final String displayName;

    ResourceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
