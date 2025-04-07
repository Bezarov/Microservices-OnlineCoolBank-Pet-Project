package com.example.userscomponent.service;

import com.example.userscomponent.dto.UsersDTO;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
public class KafkaUsersServiceImpl implements KafkaUsersService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaUsersServiceImpl.class);
    private final PasswordEncoder passwordEncoder;
    private final UsersRepository usersRepository;
    private final KafkaTemplate<String, UsersDTO> responseDTOKafkaTemplate;
    private final KafkaTemplate<String, String> responseMessageKafkaTemplate;

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
        logger.info("Got request from kafka topic: create-user with correlation id: {} ", correlationId);
        logger.info("Trying to find User with email: {}", usersDTO.getEmail());
        usersRepository.findByEmail(usersDTO.getEmail())
                .ifPresent(userEntity -> {
                    logger.error("User with such email already exists: {},", usersDTO.getEmail());
                    throw new ResponseStatusException(HttpStatus.FOUND, "User with such email: "
                            + usersDTO.getEmail() + " already exist correlationId:" + correlationId);
                });
        logger.info("User email is unique, trying to create User in DB");
        Users userEntity = usersRepository.save(convertUsersDTOToModel(usersDTO));
        logger.info("User created successfully: {}", userEntity);

        logger.info("Trying to create topic: create-user-response with correlation id: {} ", correlationId);
        ProducerRecord<String, UsersDTO> responseTopic = new ProducerRecord<>(
                "create-user-response", null, convertUsersModelToDTO(userEntity));
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-user-by-id", groupId = "users-components",
            containerFactory = "uuidKafkaListenerFactory")
    public void getUserById(UUID userId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: get-user-by-id with correlation id: {} ", correlationId);
        logger.info("Trying to find User with ID: {}", userId);
        UsersDTO responseUserDTO = usersRepository.findById(userId)
                .map(userEntity -> {
                    logger.info("User was found and received to Kafka Broker: {}", userEntity);
                    return convertUsersModelToDTO(userEntity);
                })
                .orElseThrow(() -> {
                    logger.error("User with such ID was not found: {}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found correlationId:" + correlationId);
                });

        logger.info("Trying to create topic: get-user-by-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, UsersDTO> responseTopic = new ProducerRecord<>(
                "get-user-by-id-response", null, responseUserDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-user-by-email", groupId = "users-components",
            containerFactory = "stringKafkaListenerFactory")
    public void getUserByEmail(String userEmail, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: get-user-by-email with correlation id: {} ", correlationId);
        logger.info("Trying to find User with email: {}", userEmail);
        UsersDTO responseUserDTO = usersRepository.findByEmail(userEmail)
                .map(userEntity -> {
                    logger.info("User was found and received to the Controller: {}", userEntity);
                    return convertUsersModelToDTO(userEntity);
                })
                .orElseThrow(() -> {
                    logger.error("User with such email was not found: {}", userEmail);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such email: " + userEmail + " was not found correlationId:" + correlationId);
                });

        logger.info("Trying to create topic: get-user-by-email-response with correlation id: {} ", correlationId);
        ProducerRecord<String, UsersDTO> responseTopic = new ProducerRecord<>(
                "get-user-by-email-response", null, responseUserDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-user-by-full-name", groupId = "users-components",
            containerFactory = "stringKafkaListenerFactory")
    public void getUserByFullName(String userFullName, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: get-user-by-full-name with correlation id: {} ", correlationId);
        logger.info("Trying to find User with name: {}", userFullName);
        UsersDTO responseUserDTO = usersRepository.findByFullName(userFullName)
                .map(userEntity -> {
                    logger.info("User was found and received to the Controller: {}", userEntity);
                    return convertUsersModelToDTO(userEntity);
                })
                .orElseThrow(() -> {
                    logger.error("User with such name was not found: {}", userFullName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such full name: " + userFullName + " was not found correlationId:" + correlationId);
                });

        logger.info("Trying to create topic: get-user-by-full-name-response with correlation id: {} ", correlationId);
        ProducerRecord<String, UsersDTO> responseTopic = new ProducerRecord<>(
                "get-user-by-full-name-response", null, responseUserDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-user-by-phone-number", groupId = "users-components",
            containerFactory = "stringKafkaListenerFactory")
    public void getUserByPhoneNumber(String userPhoneNumber, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: get-user-by-phone-number with correlation id: {} ", correlationId);
        logger.info("Trying to find User with phone number: {}", userPhoneNumber);
        UsersDTO responseUserDTO = usersRepository.findByPhoneNumber(userPhoneNumber)
                .map(userEntity -> {
                    logger.info("User was found and received to the Controller: {}", userEntity);
                    return convertUsersModelToDTO(userEntity);
                })
                .orElseThrow(() -> {
                    logger.error("User with such phone number was not found: {}", userPhoneNumber);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such phone number: " + userPhoneNumber + " was not found correlationId:" + correlationId);
                });

        logger.info("Trying to create topic: get-user-by-phone-number-response with correlation id: {} ", correlationId);
        ProducerRecord<String, UsersDTO> responseTopic = new ProducerRecord<>(
                "get-user-by-phone-number-response", null, responseUserDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "update-user-by-id", containerFactory = "mapUUIDToDTOKafkaListenerFactory")
    public void updateUser(Map<String, UsersDTO> mapUUIDToDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: update-user-by-id with correlation id: {} ", correlationId);
        ObjectMapper objectMapper = new ObjectMapper();
        String userId = mapUUIDToDTO.keySet().iterator().next().replaceAll("\"", "");
        UsersDTO usersDTO = objectMapper.convertValue(mapUUIDToDTO.get(userId), UsersDTO.class);

        logger.info("Trying to find User with ID: {}", userId);
        UsersDTO responseUserDTO = usersRepository.findById(UUID.fromString(userId))
                .map(userEntity -> {
                    logger.info("User was found updating");
                    userEntity.setFullName(usersDTO.getFullName());
                    userEntity.setEmail(usersDTO.getEmail());
                    userEntity.setPhoneNumber(usersDTO.getPhoneNumber());
                    userEntity.setPassword(passwordEncoder.encode(usersDTO.getPassword()));
                    logger.info("User updated successfully, trying to save in DB");
                    usersRepository.save(userEntity);
                    logger.info("User updated successfully: {}", userEntity);
                    return convertUsersModelToDTO(userEntity);
                })
                .orElseThrow(() -> {
                    logger.error("User with such ID was not found: {}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found correlationId:" + correlationId);
                });

        logger.info("Trying to create topic: update-user-by-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, UsersDTO> responseTopic = new ProducerRecord<>(
                "update-user-by-id-response", null, responseUserDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "update-user-password-by-id", containerFactory = "mapUUIDToStringKafkaListenerFactory")
    public void updatePasswordById(Map<String, String> mapUUIDToString, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: update-user-password-by-id with correlation id: {} ", correlationId);
        String userId = mapUUIDToString.keySet().iterator().next().replaceAll("\"", "");
        String newPassword = mapUUIDToString.get(userId);

        logger.info("Trying to find User with ID: {}", userId);
        UsersDTO responseUserDTO = usersRepository.findById(UUID.fromString(userId))
                .map(userEntity -> {
                    logger.info("User was found, updating password");
                    userEntity.setPassword(passwordEncoder.encode(newPassword));
                    logger.info("Password updated successfully, trying to save in DB");
                    usersRepository.save(userEntity);
                    logger.info("User password updated successfully: {}", userEntity);
                    return convertUsersModelToDTO(userEntity);
                })
                .orElseThrow(() -> {
                    logger.error("User with such ID was not found: {}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found correlationId:" + correlationId);
                });

        logger.info("Trying to create topic: update-user-password-by-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, UsersDTO> responseTopic = new ProducerRecord<>(
                "update-user-password-by-id-response", null, responseUserDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Transactional
    @Override
    @KafkaListener(topics = "delete-user-by-id", containerFactory = "uuidKafkaListenerFactory")
    public void deleteUserById(UUID userId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: delete-user-by-id with correlation id: {} ", correlationId);
        logger.info("Trying to find User with ID: {}", userId);
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User with such ID was not found: {}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found correlationId:" + correlationId);
                });
        usersRepository.deleteById(userId);
        logger.info("User was found and deleted successfully: {}", user);

        logger.info("Trying to create topic: delete-user-by-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, String> responseTopic = new ProducerRecord<>(
                "delete-user-by-id-response", null, "User deleted successfully");
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseMessageKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Transactional
    @Override
    @KafkaListener(topics = "delete-user-by-email", containerFactory = "stringKafkaListenerFactory")
    public void deleteUserByEmail(String userEmail, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: delete-user-by-email with correlation id: {} ", correlationId);
        logger.info("Trying to find User with email: {}", userEmail.replaceAll("\"", ""));
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    logger.error("User with such email was not found: {}", userEmail);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such email: " + userEmail + " was not found correlationId:" + correlationId);
                });
        usersRepository.deleteByEmail(userEmail);
        logger.info("User was found and deleted successfully: {}", user);

        logger.info("Trying to create topic: delete-user-by-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, String> responseTopic = new ProducerRecord<>(
                "delete-user-by-id-response", null, "User deleted successfully");
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseMessageKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Transactional
    @Override
    @KafkaListener(topics = "delete-user-by-full-name", containerFactory = "stringKafkaListenerFactory")
    public void deleteUserByFullName(String userFullName, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: delete-user-by-full-name with correlation id: {} ", correlationId);
        logger.info("Trying to find User with name: {}", userFullName);
        Users user = usersRepository.findByFullName(userFullName.replaceAll("\"", ""))
                .orElseThrow(() -> {
                    logger.error("User with such name was not found: {}", userFullName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such full name: " + userFullName + " was not found correlationId:" + correlationId);
                });
        usersRepository.deleteByFullName(userFullName);
        logger.info("User was found and deleted successfully: {}", user);

        logger.info("Trying to create topic: delete-user-by-full-name-response with correlation id: {} ", correlationId);
        ProducerRecord<String, String> responseTopic = new ProducerRecord<>(
                "delete-user-by-full-name-response", null, "User deleted successfully");
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseMessageKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }
}