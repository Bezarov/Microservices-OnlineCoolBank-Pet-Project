package com.example.accountcomponent.dto;

public record TokenAuthRequestDTO(String jwtToken, String requestURI) {
}
