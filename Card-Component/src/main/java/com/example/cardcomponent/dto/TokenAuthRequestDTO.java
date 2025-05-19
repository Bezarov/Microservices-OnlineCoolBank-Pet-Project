package com.example.cardcomponent.dto;

public record TokenAuthRequestDTO(String jwtToken, String requestURI) {
}