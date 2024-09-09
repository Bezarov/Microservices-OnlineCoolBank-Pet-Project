package com.example.accountcomponent.dto;

public record TokenAuthRequestDTO(String jwtToken, String requestURI) {
    @Override
    public String toString() {
        return "TokenAuthRequestDTO{" +
                "jwtToken='" + jwtToken + '\'' +
                ", requestURI='" + requestURI + '\'' +
                '}';
    }
}
