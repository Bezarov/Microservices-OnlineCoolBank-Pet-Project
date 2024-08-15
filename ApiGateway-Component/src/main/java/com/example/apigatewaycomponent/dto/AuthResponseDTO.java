package com.example.apigatewaycomponent.dto;

public record AuthResponseDTO(String token) {

    @Override
    public String toString() {
        return "Token=" + token;
    }
}
