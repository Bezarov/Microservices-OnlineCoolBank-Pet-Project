package com.example.eurekaserver.dto;

public record TokenAuthRequestDTO(String jwtToken, String requestURI) {
}