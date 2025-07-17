package com.example.appregistrycomponent.dto;

public record AuthResponseDTO(String token) {
    @Override
    public String toString() {
        return "token=" + token;
    }
}