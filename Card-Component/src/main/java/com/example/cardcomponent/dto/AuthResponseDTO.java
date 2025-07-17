package com.example.cardcomponent.dto;

public record AuthResponseDTO(String token) {
    @Override
    public String toString() {
        return "token=" + token;
    }
}