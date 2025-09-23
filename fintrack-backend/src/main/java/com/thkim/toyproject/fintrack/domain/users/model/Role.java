package com.thkim.toyproject.fintrack.domain.users.model;

public enum Role {
    USER, ADMIN;

    public String asAuthority() {
        return "ROLE_" + name();
    }
}
