package com.example.securitycomponent.dto;

public record AuthResponseDTO(String token) {

    @Override
    public String toString() {
        return token;
    }
}
