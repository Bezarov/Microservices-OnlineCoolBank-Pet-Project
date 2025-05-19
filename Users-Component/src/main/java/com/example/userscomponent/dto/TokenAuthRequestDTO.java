package com.example.userscomponent.dto;

public record TokenAuthRequestDTO(String jwtToken, String requestURI) {
}