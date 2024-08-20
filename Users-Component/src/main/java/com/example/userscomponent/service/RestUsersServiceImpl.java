package com.example.userscomponent.service;

import com.example.userscomponent.dto.UsersDTO;
import com.example.userscomponent.model.Users;
import com.example.userscomponent.repository.UsersRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class RestUsersServiceImpl implements RestUsersService {
    private static final Logger logger = LoggerFactory.getLogger(RestUsersServiceImpl.class);
    private final PasswordEncoder passwordEncoder;
    private final UsersRepository usersRepository;

    public RestUsersServiceImpl(PasswordEncoder passwordEncoder, UsersRepository usersRepository) {
        this.passwordEncoder = passwordEncoder;
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

    private Users convertUsersDTOToModel(UsersDTO usersDTO) {
        Users users = new Users();
        users.setFullName(usersDTO.getFullName());
        users.setEmail(usersDTO.getEmail());
        users.setPassword(passwordEncoder.encode(usersDTO.getPassword()));
        users.setPhoneNumber(usersDTO.getPhoneNumber());
        users.setCreatedDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        users.setStatus("ACTIVE");
        return users;
    }

    @Override
    public UsersDTO createUser(UsersDTO usersDTO) {
        logger.info("Trying to find User with email: {}", usersDTO.getEmail());
        usersRepository.findByEmail(usersDTO.getEmail())
                .ifPresent(UserEntity -> {
                    logger.error("User with such email already exists: {},", usersDTO.getEmail());
                    throw new ResponseStatusException(HttpStatus.FOUND,
                            "User with such email: " + usersDTO.getEmail() + " already exist");
                });
        logger.info("User email is unique, trying to create User in DB");
        Users user = usersRepository.save(convertUsersDTOToModel(usersDTO));
        logger.info("User created successfully: {}", user);
        return convertUsersModelToDTO(user);
    }

    @Override
    public UsersDTO getUserById(UUID userId) {
        logger.info("Trying to find User with ID: {}", userId);
        UsersDTO usersDTO = usersRepository.findById(userId)
                .map(UserEntity -> {
                    logger.info("User was found and received to the Controller: {}", UserEntity);
                    return convertUsersModelToDTO(UserEntity);
                })
                .orElseThrow(() -> {
                    logger.error("User with such ID was not found: {}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found");
                });
        return usersDTO;
    }

    @Override
    public UsersDTO getUserByEmail(String userEmail) {
        logger.info("Trying to find User with email: {}", userEmail);
        UsersDTO usersDTO = usersRepository.findByEmail(userEmail)
                .map(UserEntity -> {
                    logger.info("User was found and received to the Controller: {}", UserEntity);
                    return convertUsersModelToDTO(UserEntity);
                })
                .orElseThrow(() -> {
                    logger.error("User with such email was not found: {}", userEmail);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such email: " + userEmail + " was not found");
                });
        return usersDTO;
    }

    @Override
    public UsersDTO getUserByFullName(String userFullName) {
        logger.info("Trying to find User with name: {}", userFullName);
        UsersDTO usersDTO = usersRepository.findByFullName(userFullName)
                .map(UserEntity -> {
                    logger.info("User was found and received to the Controller: {}", UserEntity);
                    return convertUsersModelToDTO(UserEntity);
                })
                .orElseThrow(() -> {
                    logger.error("User with such name was not found: {}", userFullName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such full name: " + userFullName + " was not found");
                });
        return usersDTO;
    }

    @Override
    public UsersDTO getUserByPhoneNumber(String userPhoneNumber) {
        logger.info("Trying to find User with phone number: {}", userPhoneNumber);
        UsersDTO usersDTO = usersRepository.findByPhoneNumber(userPhoneNumber)
                .map(UserEntity -> {
                    logger.info("User was found and received to the Controller: {}", UserEntity);
                    return convertUsersModelToDTO(UserEntity);
                })
                .orElseThrow(() -> {
                    logger.error("User with such phone number was not found: {}", userPhoneNumber);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such phone number: " + userPhoneNumber + " was not found");
                });
        return usersDTO;
    }

    @Override
    public UsersDTO updateUser(UUID userId, UsersDTO usersDTO) {
        logger.info("Trying to find User with ID: {}", userId);
        return usersRepository.findById(userId)
                .map(UserEntity -> {
                    logger.info("User was found updating");
                    UserEntity.setFullName(usersDTO.getFullName());
                    UserEntity.setEmail(usersDTO.getEmail());
                    UserEntity.setPhoneNumber(usersDTO.getPhoneNumber());
                    UserEntity.setPassword(passwordEncoder.encode(usersDTO.getPassword()));
                    logger.info("User updated successfully, trying to save in DB");
                    usersRepository.save(UserEntity);
                    logger.info("User updated successfully: {}", UserEntity);
                    return convertUsersModelToDTO(UserEntity);
                })
                .orElseThrow(() -> {
                    logger.error("User with such ID was not found: {}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found");
                });
    }

    @Override
    public UsersDTO updatePasswordById(UUID userId, String newPassword) {
        logger.info("Trying to find User with ID: {}", userId);
        return usersRepository.findById(userId)
                .map(UserEntity -> {
                    logger.info("User was found, updating password");
                    UserEntity.setPassword(passwordEncoder.encode(newPassword));
                    logger.info("Password updated successfully, trying to save in DB");
                    usersRepository.save(UserEntity);
                    logger.info("User password updated successfully: {}", UserEntity);
                    return convertUsersModelToDTO(UserEntity);
                })
                .orElseThrow(() -> {
                    logger.error("User with such ID was not found: {}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found");
                });
    }

    @Transactional
    @Override
    public ResponseEntity<String> deleteUserById(UUID userId) {
        logger.info("Trying to find User with ID: {}", userId);
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User with such ID was not found: {}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found");
                });
        usersRepository.deleteById(userId);
        logger.info("User was found and deleted successfully: {}", user);
        return new ResponseEntity<>("User deleted successfully", HttpStatus.ACCEPTED);
    }

    @Transactional
    @Override
    public ResponseEntity<String> deleteUserByEmail(String userEmail) {
        logger.info("Trying to find User with email: {}", userEmail);
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    logger.error("User with such email was not found: {}", userEmail);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such email: " + userEmail + " was not found");
                });
        usersRepository.deleteByEmail(userEmail);
        logger.info("User was found and deleted successfully: {}", user);
        return new ResponseEntity<>("User deleted successfully", HttpStatus.ACCEPTED);
    }

    @Transactional
    @Override
    public ResponseEntity<String> deleteUserByFullName(String userFullName) {
        logger.info("Trying to find User with name: {}", userFullName);
        Users user = usersRepository.findByFullName(userFullName)
                .orElseThrow(() -> {
                    logger.error("User with such name was not found: {}", userFullName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such full name: " + userFullName + " was not found");
                });
        usersRepository.deleteByFullName(userFullName);
        logger.info("User was found and deleted successfully: {}", user);
        return new ResponseEntity<>("User deleted successfully", HttpStatus.ACCEPTED);
    }
}