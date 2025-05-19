package com.example.userscomponent.service;

import com.example.userscomponent.dto.UsersDTO;
import com.example.userscomponent.model.Users;
import com.example.userscomponent.repository.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class RestUsersServiceImpl implements RestUsersService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestUsersServiceImpl.class);
    private static final String USER_SEARCHING_LOG = "Trying to find User by: {}";
    private static final String USER_NOT_FOUND_LOG = "User was not found by: {}";
    private static final String USER_NAME_NOT_FOUND_LOG = "User name was not found by: {}";
    private static final String USER_FOUND_LOG = "User was found and received to the Controller: {}";

    private final UsersRepository usersRepository;

    public RestUsersServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    private UsersDTO convertUsersModelToDTO(Users user) {
        UsersDTO usersDTO = new UsersDTO();
        usersDTO.setId(user.getId());
        usersDTO.setFullName(user.getFullName());
        usersDTO.setEmail(user.getEmail());
        usersDTO.setPhoneNumber(user.getPhoneNumber());
        usersDTO.setPassword(user.getPassword());
        return usersDTO;
    }

    @Override
    public UsersDTO getUserById(UUID userId) {
        LOGGER.info(USER_SEARCHING_LOG, userId);
        return usersRepository.findById(userId)
                .map(userEntity -> {
                    LOGGER.info(USER_FOUND_LOG, userEntity);
                    return convertUsersModelToDTO(userEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found");
                });
    }

    @Override
    public UsersDTO getUserByEmail(String userEmail) {
        LOGGER.info(USER_SEARCHING_LOG, userEmail);
        return usersRepository.findByEmail(userEmail)
                .map(userEntity -> {
                    LOGGER.info(USER_FOUND_LOG, userEntity);
                    return convertUsersModelToDTO(userEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, userEmail);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such email: " + userEmail + " was not found");
                });
    }

    @Override
    public UsersDTO getUserByFullName(String userFullName) {
        LOGGER.info(USER_SEARCHING_LOG, userFullName);
        return usersRepository.findByFullNameIgnoreCase(userFullName)
                .map(userEntity -> {
                    LOGGER.info(USER_FOUND_LOG, userEntity);
                    return convertUsersModelToDTO(userEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, userFullName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such full name: " + userFullName + " was not found");
                });
    }

    @Override
    public UsersDTO getUserByPhoneNumber(String userPhoneNumber) {
        LOGGER.info(USER_SEARCHING_LOG, userPhoneNumber);
        return usersRepository.findByPhoneNumber(userPhoneNumber)
                .map(userEntity -> {
                    LOGGER.info(USER_FOUND_LOG, userEntity);
                    return convertUsersModelToDTO(userEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, userPhoneNumber);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such phone number: " + userPhoneNumber + " was not found");
                });
    }

    @Override
    public String getFullNameById(UUID userId) {
        LOGGER.info(USER_SEARCHING_LOG, userId);
        return usersRepository.findById(userId)
                .map(userEntity -> {
                    LOGGER.info(USER_FOUND_LOG, userEntity);
                    return userEntity.getFullName();
                })
                .orElseThrow(() -> {
                    LOGGER.error(USER_NAME_NOT_FOUND_LOG, userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User name with such ID: " + userId + " was not found");
                });
    }
}