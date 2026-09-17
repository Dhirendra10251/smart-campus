package com.campus.model.enums;

/**
 * Priority levels for service requests.
 * Unit 2 — Enum with a numeric level field, demonstrating enum with fields/methods.
 */
public enum Priority {
    LOW(1), MEDIUM(2), HIGH(3), CRITICAL(4);

    private final int level;

    Priority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
