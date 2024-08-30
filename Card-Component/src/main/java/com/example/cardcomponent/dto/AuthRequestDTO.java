package com.example.cardcomponent.dto;

public record AuthRequestDTO(Object principal, Object credentials) {
    @Override
    public String toString() {
        return "AuthRequestDTO{" +
                "principal=" + principal +
                ", credentials=" + credentials +
                '}';
    }
}
