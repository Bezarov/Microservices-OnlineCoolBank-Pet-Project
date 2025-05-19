package com.example.accountcomponent.dto;

import java.util.UUID;

public record UsersDTO(UUID id, String fullName, String email, String phoneNumber, String password) {
}