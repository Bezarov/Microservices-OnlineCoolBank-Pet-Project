package com.example.paymentcomponent.dto;

public record TokenAuthRequestDTO(String jwtToken, String requestURI) {
}