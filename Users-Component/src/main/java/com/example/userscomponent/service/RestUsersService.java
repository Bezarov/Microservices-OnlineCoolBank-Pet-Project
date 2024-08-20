package com.example.userscomponent.service;

import com.example.userscomponent.dto.UsersDTO;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface RestUsersService {
    UsersDTO createUser(UsersDTO usersDTO);

    UsersDTO getUserById(UUID userId);

    UsersDTO getUserByEmail(String userEmail);

    UsersDTO getUserByFullName(String userFullName);

    UsersDTO getUserByPhoneNumber(String userPhoneNumber);

    UsersDTO updateUser(UUID userId, UsersDTO usersDTO);

    UsersDTO updatePasswordById(UUID userId, String newPassword);

    ResponseEntity<String> deleteUserById(UUID userId);

    ResponseEntity<String> deleteUserByEmail(String userEmail);

    ResponseEntity<String> deleteUserByFullName(String userFullName);
}
