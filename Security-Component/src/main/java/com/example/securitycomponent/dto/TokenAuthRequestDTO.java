package com.example.securitycomponent.dto;

public record TokenAuthRequestDTO(String jwtToken, String requestURI) {
    @Override
    public String toString() {
        return "TokenAuthRequestDTO{" +
                "jwtToken='" + jwtToken + '\'' +
                ", requestURI='" + requestURI + '\'' +
                '}';
    }
}
