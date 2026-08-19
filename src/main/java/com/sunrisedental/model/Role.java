package com.sunrisedental.model;

/**
 * Enum representing user roles in the system.
 */
public enum Role {
    ADMIN("ADMIN", "System Administrator"),
    RECEPTIONIST("RECEPTIONIST", "Reception Staff"),
    DENTIST("DENTIST", "Dentist");

    private final String code;
    private final String displayName;

    Role(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromCode(String code) {
        for (Role role : Role.values()) {
            if (role.getCode().equalsIgnoreCase(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role code: " + code);
    }
}