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
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class KafkaUsersServiceImpl implements KafkaUsersService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaUsersServiceImpl.class);
    private final PasswordEncoder passwordEncoder;
    private final UsersRepository usersRepository;
    private final KafkaTemplate<String, UsersDTO> responseDTOKafkaTemplate;
//    private final KafkaTemplate<String, ResponseEntity<String>> responseMessageKafkaTemplate;

    @Autowired
    public KafkaUsersServiceImpl(PasswordEncoder passwordEncoder, UsersRepository usersRepository,
                                 KafkaTemplate<String, UsersDTO> responseDTOKafkaTemplate) {
        this.passwordEncoder = passwordEncoder;
        this.usersRepository = usersRepository;
        this.responseDTOKafkaTemplate = responseDTOKafkaTemplate;
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
    @KafkaListener(topics = "create-user", groupId = "users-component",
            containerFactory = "usersDTOKafkaListenerFactory")
    public void createUser(UsersDTO usersDTO,
                           @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Trying to find User with email: {}", usersDTO.getEmail());
        usersRepository.findByEmail(usersDTO.getEmail())
                .ifPresent(UserEntity -> {
                    logger.error("User with such email already exists: {},", usersDTO.getEmail());
                    throw new ResponseStatusException(HttpStatus.FOUND, "User with such email: "
                            + usersDTO.getEmail() + " already exist correlationId:" + correlationId);
                });
        logger.info("User email is unique, trying to create User in DB");
        Users userEntity = usersRepository.save(convertUsersDTOToModel(usersDTO));
        logger.info("User created successfully: {}", userEntity);
        ProducerRecord<String, UsersDTO> responseRecord = new ProducerRecord<>(
                "create-user-response", null, convertUsersModelToDTO(userEntity));
        responseRecord.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseRecord);
    }

    @Override
    @KafkaListener(topics = "get-user-by-id", groupId = "users-component",
            containerFactory = "usersDTOKafkaListenerFactory")
    public void getUserById(String userId,
                            @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Trying to find User with ID: {}", userId);
        UsersDTO responseUserDTO = usersRepository.findById(UUID.fromString(userId.replaceAll("\"", "")))
                .map(UserEntity -> {
                    logger.info("User was found and received to Kafka Broker: {}", UserEntity);
                    return convertUsersModelToDTO(UserEntity);
                })
                .orElseThrow(() -> {
                    logger.error("User with such ID was not found: {}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found correlationId:" + correlationId);
                });
        ProducerRecord<String, UsersDTO> responseRecord = new ProducerRecord<>(
                "get-user-by-id-response", null, responseUserDTO);
        responseRecord.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseRecord);
    }

    @Override
    @KafkaListener(topics = "get-user-by-email", groupId = "users-component",
            containerFactory = "usersDTOKafkaListenerFactory")
    public void getUserByEmail(String userEmail,
                               @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Trying to find User with email: {}", userEmail);
        UsersDTO responseUserDTO = usersRepository.findByEmail(userEmail.replaceAll("\"", ""))
                .map(UserEntity -> {
                    logger.info("User was found and received to the Controller: {}", UserEntity);
                    return convertUsersModelToDTO(UserEntity);
                })
                .orElseThrow(() -> {
                    logger.error("User with such email was not found: {}", userEmail);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such email: " + userEmail + " was not found correlationId:" + correlationId);
                });
        ProducerRecord<String, UsersDTO> responseRecord = new ProducerRecord<>(
                "get-user-by-email-response", null, responseUserDTO);
        responseRecord.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseRecord);
    }

    @Override
    @KafkaListener(topics = "get-user-by-full-name", groupId = "users-component",
            containerFactory = "usersDTOKafkaListenerFactory")
    public void getUserByFullName(String userFullName,
                                  @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Trying to find User with name: {}", userFullName);
        UsersDTO responseUserDTO = usersRepository.findByFullName(userFullName.replaceAll("\"", ""))
                .map(UserEntity -> {
                    logger.info("User was found and received to the Controller: {}", UserEntity);
                    return convertUsersModelToDTO(UserEntity);
                })
                .orElseThrow(() -> {
                    logger.error("User with such name was not found: {}", userFullName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such full name: " + userFullName + " was not found correlationId:" + correlationId);
                });
        ProducerRecord<String, UsersDTO> responseRecord = new ProducerRecord<>(
                "get-user-by-full-name-response", null, responseUserDTO);
        responseRecord.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseRecord);
    }

    @Override
    @KafkaListener(topics = "get-user-by-phone-number", groupId = "users-component",
            containerFactory = "usersDTOKafkaListenerFactory")
    public void getUserByPhoneNumber(String userPhoneNumber,
                                     @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Trying to find User with phone number: {}", userPhoneNumber);
        UsersDTO responseUserDTO = usersRepository.findByPhoneNumber(userPhoneNumber.replaceAll("\"", ""))
                .map(UserEntity -> {
                    logger.info("User was found and received to the Controller: {}", UserEntity);
                    return convertUsersModelToDTO(UserEntity);
                })
                .orElseThrow(() -> {
                    logger.error("User with such phone number was not found: {}", userPhoneNumber);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such phone number: " + userPhoneNumber + " was not found correlationId:" + correlationId);
                });
        ProducerRecord<String, UsersDTO> responseRecord = new ProducerRecord<>(
                "get-user-by-phone-number-response", null, responseUserDTO);
        responseRecord.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseRecord);
    }

    @Override
    @KafkaListener(topics = "update-user-by-id", groupId = "users-component",
            containerFactory = "mapKafkaListenerFactory")
    public void updateUser(Map<String, Object> UUIDAndUserDTOMap,
                           @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        ObjectMapper objectMapper = new ObjectMapper();
        String userId = UUIDAndUserDTOMap.keySet().iterator().next();
        LinkedHashMap<String, Object> userDTOMap = (LinkedHashMap<String, Object>) UUIDAndUserDTOMap.get(userId);
        UsersDTO usersDTO = objectMapper.convertValue(userDTOMap, UsersDTO.class);

        logger.info("Trying to find User with ID: {}", userId);
        UsersDTO responseUserDTO = usersRepository.findById(UUID.fromString(userId.replaceAll("\"", "")))
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
                            "User with such ID: " + userId + " was not found correlationId:" + correlationId);
                });
        ProducerRecord<String, UsersDTO> responseRecord = new ProducerRecord<>(
                "update-user-by-id-response", null, responseUserDTO);
        responseRecord.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseRecord);
    }

    @Override
    @KafkaListener(topics = "update-user-password-by-id", groupId = "users-component",
            containerFactory = "mapKafkaListenerFactory")
    public void updatePasswordById(Map<String, String> UUIDAndNewPasswordMap,
                                   @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        String userId = UUIDAndNewPasswordMap.keySet().iterator().next();
        String newPassword = UUIDAndNewPasswordMap.get(userId);

        logger.info("Trying to find User with ID: {}", userId);
        UsersDTO responseUserDTO = usersRepository.findById(UUID.fromString(userId.replaceAll("\"", "")))
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
                            "User with such ID: " + userId + " was not found correlationId:" + correlationId);
                });
        ProducerRecord<String, UsersDTO> responseRecord = new ProducerRecord<>(
                "update-user-password-by-id-response", null, responseUserDTO);
        responseRecord.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseRecord);
    }

    @Transactional
    @Override
    @KafkaListener(topics = "delete-user-by-id", groupId = "users-component",
            containerFactory = "usersDTOKafkaListenerFactory")
    public void deleteUserById(String userId,
                               @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Trying to find User with ID: {}", userId);
        Users user = usersRepository.findById(UUID.fromString(userId.replaceAll("\"", "")))
                .orElseThrow(() -> {
                    logger.error("User with such ID was not found: {}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such ID: " + userId + " was not found correlationId:" + correlationId);
                });
        usersRepository.deleteById(UUID.fromString(userId.replaceAll("\"", "")));
        logger.info("User was found and deleted successfully: {}", user);
        ResponseEntity<String> responseEntity =
                new ResponseEntity<>("User deleted successfully", HttpStatus.ACCEPTED);
//        ProducerRecord<String, ResponseEntity<String> > responseRecord = new ProducerRecord<>(
//                "delete-user-by-id-response", null, responseEntity);
//        responseRecord.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
//        responseMessageKafkaTemplate.send(responseRecord);
    }

    @Transactional
    @Override
    @KafkaListener(topics = "delete-user-by-email", groupId = "users-component",
            containerFactory = "usersDTOKafkaListenerFactory")
    public void deleteUserByEmail(String userEmail,
                                  @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Trying to find User with email: {}", userEmail.replaceAll("\"", ""));
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    logger.error("User with such email was not found: {}", userEmail);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such email: " + userEmail + " was not found correlationId:" + correlationId);
                });
        usersRepository.deleteByEmail(userEmail);
        logger.info("User was found and deleted successfully: {}", user);
        ResponseEntity<String> responseEntity =
                new ResponseEntity<>("User deleted successfully", HttpStatus.ACCEPTED);
//        ProducerRecord<String, ResponseEntity<String> > responseRecord = new ProducerRecord<>(
//                "delete-user-by-email-response", null, responseEntity);
//        responseRecord.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
//        responseMessageKafkaTemplate.send(responseRecord);
    }

    @Transactional
    @Override
    @KafkaListener(topics = "delete-user-by-full-name", groupId = "users-component",
            containerFactory = "usersDTOKafkaListenerFactory")
    public void deleteUserByFullName(String userFullName,
                                     @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Trying to find User with name: {}", userFullName);
        Users user = usersRepository.findByFullName(userFullName.replaceAll("\"", ""))
                .orElseThrow(() -> {
                    logger.error("User with such name was not found: {}", userFullName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "User with such full name: " + userFullName + " was not found correlationId:" + correlationId);
                });
        usersRepository.deleteByFullName(userFullName);
        logger.info("User was found and deleted successfully: {}", user);
        ResponseEntity<String> responseEntity =
                new ResponseEntity<>("User deleted successfully", HttpStatus.ACCEPTED);
//        ProducerRecord<String, ResponseEntity<String> > responseRecord = new ProducerRecord<>(
//                "delete-user-by-full-name-response", null, responseEntity);
//        responseRecord.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
//        responseMessageKafkaTemplate.send(responseRecord);
    }
}