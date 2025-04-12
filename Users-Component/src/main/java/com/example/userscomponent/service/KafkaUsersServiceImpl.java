package com.example.userscomponent.service;

import com.example.userscomponent.dto.UsersDTO;
import com.example.userscomponent.exception.CustomKafkaException;
import com.example.userscomponent.model.Users;
import com.example.userscomponent.repository.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
public class KafkaUsersServiceImpl implements KafkaUsersService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaUsersServiceImpl.class);
    private static final String ALLOCATED_TOPIC_LOG = "Topic was created and allocated in kafka broker successfully: {}";
    private static final String USER_SEARCHING_LOG = "Trying to find User by: {}";
    private static final String USER_NOT_FOUND_LOG = "User was not found by: {}";
    private static final String FOUND_USER_LOG = "User was found in DB: {}";
    private static final String DELETED_USER_LOG = "User was found and deleted successfully: {}";

    private final KafkaTemplate<String, UsersDTO> responseDTOKafkaTemplate;
    private final KafkaTemplate<String, String> responseMessageKafkaTemplate;

    private final PasswordEncoder passwordEncoder;
    private final UsersRepository usersRepository;

    @Autowired
    public KafkaUsersServiceImpl(PasswordEncoder passwordEncoder, UsersRepository usersRepository,
                                 KafkaTemplate<String, UsersDTO> responseDTOKafkaTemplate,
                                 KafkaTemplate<String, String> responseMessageKafkaTemplate) {
        this.passwordEncoder = passwordEncoder;
        this.usersRepository = usersRepository;
        this.responseDTOKafkaTemplate = responseDTOKafkaTemplate;
        this.responseMessageKafkaTemplate = responseMessageKafkaTemplate;
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
    @KafkaListener(topics = "create-user", containerFactory = "usersDTOKafkaListenerFactory")
    public void createUser(UsersDTO usersDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: create-user with correlation id: {} ", correlationId);
        LOGGER.info(USER_SEARCHING_LOG, usersDTO.getEmail());
        usersRepository.findByEmail(usersDTO.getEmail())
                .ifPresent(userEntity -> {
                    LOGGER.error("User with such email already exists: {},", usersDTO.getEmail());
                    throw new CustomKafkaException(HttpStatus.FOUND, "User with such email: "
                            + usersDTO.getEmail() + " already exist correlationId:" + correlationId);
                });

        LOGGER.info("User email is unique, trying to create User in DB");
        Users userEntity = usersRepository.save(convertUsersDTOToModel(usersDTO));
        LOGGER.info("User created successfully: {}", userEntity);

        LOGGER.info("Trying to create topic: create-user-response with correlation id: {} ", correlationId);
        ProducerRecord<String, UsersDTO> responseTopic = new ProducerRecord<>(
                "create-user-response", null, convertUsersModelToDTO(userEntity));
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-user-by-id", groupId = "users-components",
            containerFactory = "uuidKafkaListenerFactory")
    public void getUserById(UUID userId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-user-by-id with correlation id: {} ", correlationId);
        LOGGER.info(USER_SEARCHING_LOG, userId);
        UsersDTO responseUserDTO = usersRepository.findById(userId)
                .map(userEntity -> {
                    LOGGER.info(FOUND_USER_LOG, userEntity);
                    return convertUsersModelToDTO(userEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, userId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found correlationId:" + correlationId);
                });

        LOGGER.info("Trying to create topic: get-user-by-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, UsersDTO> responseTopic = new ProducerRecord<>(
                "get-user-by-id-response", null, responseUserDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-user-by-email", groupId = "users-components",
            containerFactory = "stringKafkaListenerFactory")
    public void getUserByEmail(String userEmail, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-user-by-email with correlation id: {} ", correlationId);
        LOGGER.info(USER_SEARCHING_LOG, userEmail);
        UsersDTO responseUserDTO = usersRepository.findByEmail(userEmail)
                .map(userEntity -> {
                    LOGGER.info(FOUND_USER_LOG, userEntity);
                    return convertUsersModelToDTO(userEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, userEmail);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such email: " + userEmail + " was not found correlationId:" + correlationId);
                });

        LOGGER.info("Trying to create topic: get-user-by-email-response with correlation id: {} ", correlationId);
        ProducerRecord<String, UsersDTO> responseTopic = new ProducerRecord<>(
                "get-user-by-email-response", null, responseUserDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-user-by-full-name", groupId = "users-components",
            containerFactory = "stringKafkaListenerFactory")
    public void getUserByFullName(String userFullName, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-user-by-full-name with correlation id: {} ", correlationId);
        LOGGER.info(USER_SEARCHING_LOG, userFullName);
        UsersDTO responseUserDTO = usersRepository.findByFullName(userFullName)
                .map(userEntity -> {
                    LOGGER.info(FOUND_USER_LOG, userEntity);
                    return convertUsersModelToDTO(userEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, userFullName);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such full name: " + userFullName + " was not found correlationId:" + correlationId);
                });

        LOGGER.info("Trying to create topic: get-user-by-full-name-response with correlation id: {} ", correlationId);
        ProducerRecord<String, UsersDTO> responseTopic = new ProducerRecord<>(
                "get-user-by-full-name-response", null, responseUserDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-user-by-phone-number", groupId = "users-components",
            containerFactory = "stringKafkaListenerFactory")
    public void getUserByPhoneNumber(String userPhoneNumber, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-user-by-phone-number with correlation id: {} ", correlationId);
        LOGGER.info(USER_SEARCHING_LOG, userPhoneNumber);
        UsersDTO responseUserDTO = usersRepository.findByPhoneNumber(userPhoneNumber)
                .map(userEntity -> {
                    LOGGER.info(FOUND_USER_LOG, userEntity);
                    return convertUsersModelToDTO(userEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, userPhoneNumber);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such phone number: " + userPhoneNumber + " was not found correlationId:" + correlationId);
                });

        LOGGER.info("Trying to create topic: get-user-by-phone-number-response with correlation id: {} ", correlationId);
        ProducerRecord<String, UsersDTO> responseTopic = new ProducerRecord<>(
                "get-user-by-phone-number-response", null, responseUserDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "update-user-by-id", containerFactory = "mapUUIDToDTOKafkaListenerFactory")
    public void updateUser(Map<String, UsersDTO> mapUUIDToDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: update-user-by-id with correlation id: {} ", correlationId);
        ObjectMapper objectMapper = new ObjectMapper();
        String userId = mapUUIDToDTO.keySet().iterator().next().replaceAll("\"", "");
        UsersDTO usersDTO = objectMapper.convertValue(mapUUIDToDTO.get(userId), UsersDTO.class);

        LOGGER.info(USER_SEARCHING_LOG, userId);
        UsersDTO responseUserDTO = usersRepository.findById(UUID.fromString(userId))
                .map(userEntity -> {
                    LOGGER.info(FOUND_USER_LOG, userEntity);
                    userEntity.setFullName(usersDTO.getFullName());
                    userEntity.setEmail(usersDTO.getEmail());
                    userEntity.setPhoneNumber(usersDTO.getPhoneNumber());
                    userEntity.setPassword(passwordEncoder.encode(usersDTO.getPassword()));
                    usersRepository.save(userEntity);
                    LOGGER.info("User updated successfully: {}", userEntity);
                    return convertUsersModelToDTO(userEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, userId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found correlationId:" + correlationId);
                });

        LOGGER.info("Trying to create topic: update-user-by-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, UsersDTO> responseTopic = new ProducerRecord<>(
                "update-user-by-id-response", null, responseUserDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "update-user-password-by-id", containerFactory = "mapUUIDToStringKafkaListenerFactory")
    public void updatePasswordById(Map<String, String> mapUUIDToString, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: update-user-password-by-id with correlation id: {} ", correlationId);
        String userId = mapUUIDToString.keySet().iterator().next().replaceAll("\"", "");
        String newPassword = mapUUIDToString.get(userId);

        LOGGER.info(USER_SEARCHING_LOG, userId);
        UsersDTO responseUserDTO = usersRepository.findById(UUID.fromString(userId))
                .map(userEntity -> {
                    LOGGER.info("User was found: {}", userEntity);
                    userEntity.setPassword(passwordEncoder.encode(newPassword));
                    usersRepository.save(userEntity);
                    LOGGER.info("User password updated successfully: {}", userEntity);
                    return convertUsersModelToDTO(userEntity);
                })
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, userId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found correlationId:" + correlationId);
                });

        LOGGER.info("Trying to create topic: update-user-password-by-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, UsersDTO> responseTopic = new ProducerRecord<>(
                "update-user-password-by-id-response", null, responseUserDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Transactional
    @Override
    @KafkaListener(topics = "delete-user-by-id", containerFactory = "uuidKafkaListenerFactory")
    public void deleteUserById(UUID userId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: delete-user-by-id with correlation id: {} ", correlationId);
        LOGGER.info(USER_SEARCHING_LOG, userId);
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, userId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found correlationId:" + correlationId);
                });
        usersRepository.deleteById(userId);
        LOGGER.info(DELETED_USER_LOG, user);

        LOGGER.info("Trying to create topic: delete-user-by-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, String> responseTopic = new ProducerRecord<>(
                "delete-user-by-id-response", null, "User deleted successfully");
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseMessageKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Transactional
    @Override
    @KafkaListener(topics = "delete-user-by-email", containerFactory = "stringKafkaListenerFactory")
    public void deleteUserByEmail(String userEmail, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: delete-user-by-email with correlation id: {} ", correlationId);
        LOGGER.info("Trying to find User with email: {}", userEmail.replaceAll("\"", ""));
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    LOGGER.error(USER_NOT_FOUND_LOG, userEmail);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such email: " + userEmail + " was not found correlationId:" + correlationId);
                });
        usersRepository.deleteByEmail(userEmail);
        LOGGER.info(DELETED_USER_LOG, user);

        LOGGER.info("Trying to create topic: delete-user-by-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, String> responseTopic = new ProducerRecord<>(
                "delete-user-by-id-response", null, "User deleted successfully");
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseMessageKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Transactional
    @Override
    @KafkaListener(topics = "delete-user-by-full-name", containerFactory = "stringKafkaListenerFactory")
    public void deleteUserByFullName(String userFullName, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: delete-user-by-full-name with correlation id: {} ", correlationId);
        LOGGER.info(USER_SEARCHING_LOG, userFullName);
        Users user = usersRepository.findByFullName(userFullName)
                .orElseThrow(() -> {
                    LOGGER.error("User with such name was not found: {}", userFullName);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "User with such full name: " + userFullName + " was not found correlationId:" + correlationId);
                });
        usersRepository.deleteByFullName(userFullName);
        LOGGER.info(DELETED_USER_LOG, user);

        LOGGER.info("Trying to create topic: delete-user-by-full-name-response with correlation id: {} ", correlationId);
        ProducerRecord<String, String> responseTopic = new ProducerRecord<>(
                "delete-user-by-full-name-response", null, "User deleted successfully");
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseMessageKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }
}