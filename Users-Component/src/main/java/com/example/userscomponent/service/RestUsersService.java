package com.example.userscomponent.service;

import com.example.userscomponent.dto.UsersDTO;

import java.util.UUID;

public interface RestUsersService {

    UsersDTO getUserById(UUID userId);

    UsersDTO getUserByEmail(String userEmail);

    UsersDTO getUserByFullName(String userFullName);

    UsersDTO getUserByPhoneNumber(String userPhoneNumber);
}
