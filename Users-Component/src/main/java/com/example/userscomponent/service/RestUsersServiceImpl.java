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
    private static final Logger logger = LoggerFactory.getLogger(RestUsersServiceImpl.class);
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
        logger.info("Trying to find User with ID: {}", userId);
        return usersRepository.findById(userId)
                .map(UserEntity -> {
                    logger.info("User was found and received to the Controller: {}", UserEntity);
                    return convertUsersModelToDTO(UserEntity);
                })
                .orElseThrow(() -> {
                    logger.error("User with such ID was not found: {}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found");
                });
    }

    @Override
    public UsersDTO getUserByEmail(String userEmail) {
        logger.info("Trying to find User with email: {}", userEmail);
        return usersRepository.findByEmail(userEmail)
                .map(UserEntity -> {
                    logger.info("User was found and received to the Controller: {}", UserEntity);
                    return convertUsersModelToDTO(UserEntity);
                })
                .orElseThrow(() -> {
                    logger.error("User with such email was not found: {}", userEmail);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such email: " + userEmail + " was not found");
                });
    }

    @Override
    public UsersDTO getUserByFullName(String userFullName) {
        logger.info("Trying to find User with name: {}", userFullName);
        return usersRepository.findByFullName(userFullName)
                .map(UserEntity -> {
                    logger.info("User was found and received to the Controller: {}", UserEntity);
                    return convertUsersModelToDTO(UserEntity);
                })
                .orElseThrow(() -> {
                    logger.error("User with such name was not found: {}", userFullName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such full name: " + userFullName + " was not found");
                });
    }

    @Override
    public UsersDTO getUserByPhoneNumber(String userPhoneNumber) {
        logger.info("Trying to find User with phone number: {}", userPhoneNumber);
        return usersRepository.findByPhoneNumber(userPhoneNumber)
                .map(UserEntity -> {
                    logger.info("User was found and received to the Controller: {}", UserEntity);
                    return convertUsersModelToDTO(UserEntity);
                })
                .orElseThrow(() -> {
                    logger.error("User with such phone number was not found: {}", userPhoneNumber);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such phone number: " + userPhoneNumber + " was not found");
                });
    }
}