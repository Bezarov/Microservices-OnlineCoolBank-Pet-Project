package com.example.securitycomponent.dto;

public record TokenAuthRequestDTO(String jwtToken, String requestURI) {
}