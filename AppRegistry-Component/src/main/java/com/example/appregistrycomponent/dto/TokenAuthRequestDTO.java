package com.example.appregistrycomponent.dto;

public record TokenAuthRequestDTO(String jwtToken, String requestURI) {
}