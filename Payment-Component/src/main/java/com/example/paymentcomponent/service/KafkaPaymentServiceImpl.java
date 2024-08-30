package com.example.paymentcomponent.service;

import com.example.paymentcomponent.dto.AccountDTO;
import com.example.paymentcomponent.dto.PaymentDTO;
import com.example.paymentcomponent.feign.AccountComponentClient;
import com.example.paymentcomponent.feign.CardComponentClient;
import com.example.paymentcomponent.model.Payment;
import com.example.paymentcomponent.repository.PaymentRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class KafkaPaymentServiceImpl implements KafkaPaymentService {
    private final static Logger logger = LoggerFactory.getLogger(KafkaPaymentServiceImpl.class);
    private final AccountComponentClient accountComponentClient;
    private final CardComponentClient cardComponentClient;
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentDTO> responseDTOKafkaTemplate;
    private final KafkaTemplate<String, AccountDTO> requestDTOKafkaTemplate;
    private final KafkaTemplate<String, List<PaymentDTO>> responseDTOSKafkaTemplate;

    public KafkaPaymentServiceImpl(@Qualifier("Account-Components") AccountComponentClient accountComponentClient,
                                   @Qualifier("Card-Components") CardComponentClient cardComponentClient,
                                   PaymentRepository paymentRepository, KafkaTemplate<String, PaymentDTO> responseDTOKafkaTemplate,
                                   KafkaTemplate<String, AccountDTO> requestDTOKafkaTemplate,
                                   KafkaTemplate<String, List<PaymentDTO>> responseDTOSKafkaTemplate) {
        this.accountComponentClient = accountComponentClient;
        this.cardComponentClient = cardComponentClient;
        this.paymentRepository = paymentRepository;
        this.responseDTOKafkaTemplate = responseDTOKafkaTemplate;
        this.requestDTOKafkaTemplate = requestDTOKafkaTemplate;
        this.responseDTOSKafkaTemplate = responseDTOSKafkaTemplate;
    }

    private PaymentDTO convertPaymentModelToDTO(Payment payment) {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setId(payment.getId());
        paymentDTO.setFromAccount(payment.getFromAccountId());
        paymentDTO.setToAccount(payment.getToAccountId());
        paymentDTO.setPaymentDate(payment.getPaymentDate());
        paymentDTO.setAmount(payment.getAmount());
        paymentDTO.setStatus(payment.getStatus());
        paymentDTO.setPaymentType(payment.getPaymentType());
        paymentDTO.setDescription(payment.getDescription());
        return paymentDTO;
    }

    private Payment convertPaymentDTOToModel(UUID fromAccount, UUID toAccount, PaymentDTO paymentDTO) {
        Payment payment = new Payment();
        payment.setFromAccountId(fromAccount);
        payment.setToAccountId(toAccount);
        payment.setPaymentDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        payment.setAmount(paymentDTO.getAmount());
        payment.setDescription(paymentDTO.getDescription());
        payment.setPaymentType(paymentDTO.getPaymentType());
        payment.setStatus("COMPLETED");
        return payment;
    }

    @Override
    @KafkaListener(topics = "create-payment-by-accounts", groupId = "account-component",
            containerFactory = "paymentDTOKafkaListenerFactory")
    public void createPaymentByAccounts(PaymentDTO paymentDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: create-payment-by-accounts with correlation id: {} ", correlationId);
        logger.info("Trying to find From-Account ID: {}", paymentDTO.getFromAccount());
        AccountDTO accountDTOFromAccount = accountComponentClient.findById(paymentDTO.getFromAccount())
                .orElseThrow(() -> {
                    logger.error("From-Account ID was not found: {}", paymentDTO.getFromAccount());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "From-Account ID: " + paymentDTO.getFromAccount() + " was not found");
                });
        logger.info("Checking availability of sufficient funds From-Account ID: {}",
                paymentDTO.getFromAccount());
        if (accountDTOFromAccount.getBalance().compareTo(paymentDTO.getAmount()) < 0) {
            logger.error("Insufficient FUNDS for From-Account ID: {}", paymentDTO.getFromAccount());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "INSUFFICIENT FUNDS");
        }
        logger.info("Funds enough, Trying to debit");
        accountDTOFromAccount.setBalance(accountDTOFromAccount.getBalance().subtract(paymentDTO.getAmount()));
        logger.info("Trying to create topic: debit-funds with correlation id: {} ", correlationId);
        ProducerRecord<String, AccountDTO> debitRequestTopic = new ProducerRecord<>(
                "debit-funds", null, accountDTOFromAccount);
        debitRequestTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        requestDTOKafkaTemplate.send(debitRequestTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", debitRequestTopic.value());
        logger.info("Funds was Debited From-Account with ID: {}", accountDTOFromAccount.getId());

        logger.info("Trying to find To-Account ID: {}", paymentDTO.getToAccount());
        AccountDTO accountDTOToAccount = accountComponentClient.findById(paymentDTO.getToAccount())
                .orElseThrow(() -> {
                    logger.error("To-Account ID was not found: {}", paymentDTO.getFromAccount());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "To-Account ID: " + paymentDTO.getToAccount() + " was not found");
                });
        logger.info("Trying to credit Account-To");
        accountDTOToAccount.setBalance(accountDTOToAccount.getBalance().add(paymentDTO.getAmount()));
        logger.info("Trying to create topic: credit-funds with correlation id: {} ", correlationId);
        ProducerRecord<String, AccountDTO> creditRequestTopic = new ProducerRecord<>(
                "credit-funds", null, accountDTOFromAccount);
        creditRequestTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        requestDTOKafkaTemplate.send(creditRequestTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", creditRequestTopic.value());
        logger.info("Funds was Credited to To-Account ID: {}", accountDTOToAccount.getId());


        paymentDTO.setPaymentType("Account to Account Transfer");
        logger.info("Payment created successfully, saving in DB");
        Payment payment = paymentRepository.save(convertPaymentDTOToModel(
                accountDTOFromAccount.getId(), accountDTOToAccount.getId(), paymentDTO));
        logger.info("Account to Account transaction ended successfully: {}", payment);

        logger.info("Trying to create topic: create-payment-by-accounts-response with correlation id: {} ", correlationId);
        ProducerRecord<String, PaymentDTO> responseTopic = new ProducerRecord<>(
                "create-payment-by-accounts-response", null, convertPaymentModelToDTO(payment));
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "create-payment-by-cards", groupId = "account-component",
            containerFactory = "listOfObjectsKafkaListenerFactory")
    public void createPaymentByCards(String fromCardNumber, String toCardNumber, BigDecimal amount,
                                     @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: create-payment-by-cards with correlation id: {} ", correlationId);
        logger.info("Trying to find Account by Card Number: {}, from which funds will be Debited", fromCardNumber);
        AccountDTO accountDTOFromAccount = cardComponentClient.findByCardNumber(fromCardNumber)
                .map(cardDTO -> {
                    logger.info("From Card was found successfully by card number: {}", cardDTO);
                    logger.info("Trying to find Linked Account to this Card");
                    AccountDTO fromAccountDTO = accountComponentClient.findById(cardDTO.getAccountId())
                            .orElseThrow(() -> {
                                logger.error("Linked Account to this Card Number was not found: {}", fromCardNumber);
                                return new ResponseStatusException(HttpStatus.NOT_FOUND,
                                        "Linked Account to this Card Number: " + fromCardNumber + " was not found");
                            });
                    logger.info("Account was found successfully: {}", fromAccountDTO);
                    return fromAccountDTO;
                })
                .orElseThrow(() -> {
                    logger.error("Card FROM with card-number: {} was not found", fromCardNumber);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Card with such ID: " + fromCardNumber + " was not found");
                });
        logger.info("Checking availability of sufficient funds From-Account ID: {}", accountDTOFromAccount.getId());
        if (accountDTOFromAccount.getBalance().compareTo(amount) < 0) {
            logger.error("Insufficient FUNDS for From-Account ID: {}", accountDTOFromAccount.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INSUFFICIENT FUNDS");
        }
        logger.info("Funds enough, Trying to debit");
        accountDTOFromAccount.setBalance(accountDTOFromAccount.getBalance().subtract(amount));
        logger.info("Trying to create topic: debit-funds with correlation id: {} ", correlationId);
        ProducerRecord<String, AccountDTO> debitRequestTopic = new ProducerRecord<>(
                "debit-funds", null, accountDTOFromAccount);
        debitRequestTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        requestDTOKafkaTemplate.send(debitRequestTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", debitRequestTopic.value());
        logger.info("Funds was Debited From-Account with ID: {}", accountDTOFromAccount.getId());

        logger.info("Trying to find Account by Card Number: {}, on which funds will be Credited",
                toCardNumber);
        AccountDTO accountDTOToAccount = cardComponentClient.findByCardNumber(toCardNumber)
                .map(cardDTO -> {
                    logger.info("To Card was found successfully by card number: {}", cardDTO);
                    logger.info("Trying to find Linked Account to this Card");
                    AccountDTO toAccountDTO = accountComponentClient.findById(cardDTO.getAccountId())
                            .orElseThrow(() -> {
                                logger.error("Linked Account to this Card Number was not found: {}", toCardNumber);
                                return new ResponseStatusException(HttpStatus.NOT_FOUND,
                                        "Linked Account to this Card Number: " + toCardNumber + " was not found");
                            });
                    logger.info("Account was found successfully: {}", toAccountDTO);
                    return toAccountDTO;
                })
                .orElseThrow(() -> {
                    logger.error("Card TO with card-number: {} was not found", toCardNumber);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Card with such ID: " + toCardNumber + " was not found");
                });
        logger.info("Trying to credit Account-To");
        accountDTOToAccount.setBalance(accountDTOToAccount.getBalance().add(amount));
        logger.info("Trying to create topic: credit-funds with correlation id: {} ", correlationId);
        ProducerRecord<String, AccountDTO> creditRequestTopic = new ProducerRecord<>(
                "credit-funds", null, accountDTOFromAccount);
        creditRequestTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        requestDTOKafkaTemplate.send(creditRequestTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", creditRequestTopic.value());
        logger.info("Funds was Credited to To-Account ID: {}", accountDTOToAccount.getId());

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setFromAccount(accountDTOFromAccount.getId());
        paymentDTO.setToAccount(accountDTOToAccount.getId());
        paymentDTO.setAmount(amount);
        paymentDTO.setPaymentType("Card to Card Transfer");
        paymentDTO.setDescription("Transfer from Card: " + fromCardNumber + " to Card: " + toCardNumber);
        logger.info("Payment created successfully, saving in DB");
        Payment payment = paymentRepository.save(convertPaymentDTOToModel(
                accountDTOFromAccount.getId(), accountDTOToAccount.getId(), paymentDTO));
        logger.info("Card to Card transaction ended successfully: {}", payment);

        logger.info("Trying to create topic: create-payment-by-cards-response with correlation id: {} ", correlationId);
        ProducerRecord<String, PaymentDTO> responseTopic = new ProducerRecord<>(
                "create-payment-by-cards-response", null, convertPaymentModelToDTO(payment));
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-payment-by-id", groupId = "account-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void getPaymentById(UUID paymentId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: get-payment-by-id with correlation id: {} ", correlationId);
        logger.info("Trying to find Payment with ID: {}", paymentId);
        PaymentDTO paymentDTO = paymentRepository.findById(paymentId)
                .map(PaymentEntity -> {
                    logger.info("Payment was found and received to the Controller: {}", PaymentEntity);
                    return convertPaymentModelToDTO(PaymentEntity);
                })
                .orElseThrow(() -> {
                    logger.error("Payment with such ID: {} was not found", paymentId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Payment with such ID was NOT Found: " + paymentId);
                });

        logger.info("Trying to create topic: get-payment-by-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, PaymentDTO> responseTopic = new ProducerRecord<>(
                "get-payment-by-id-response", null, paymentDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-all-payments-by-from-account-id", groupId = "account-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void getAllAccountPaymentsByFromAccount(UUID fromAccountId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: get-all-payments-by-from-account-id with correlation id: {} ", correlationId);
        logger.info("Trying to find From-Account ID: {}", fromAccountId);
        accountComponentClient.findById(fromAccountId)
                .orElseThrow(() -> {
                    logger.error("Account with such ID was not found: {}", fromAccountId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + fromAccountId + " was not found");
                });

        logger.info("Trying to find all Account Payments with Account ID: {}", fromAccountId);
        List<PaymentDTO> paymentDTOS = paymentRepository.findAllByFromAccountId(fromAccountId).stream()
                .map(this::convertPaymentModelToDTO)
                .toList();
        logger.info("Payments was found successfully: {}", paymentDTOS);

        logger.info("Trying to create topic: get-all-payment-by-from-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<PaymentDTO>> responseTopic = new ProducerRecord<>(
                "get-all-payment-by-from-account-id-response", null, paymentDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOSKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-payments-by-status", groupId = "account-component",
            containerFactory = "mapObjectToObjectKafkaListenerFactory")
    public void getPaymentsByStatus(Map<String, String> mapFromAccountIdToStatus, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: get-payments-by-status with correlation id: {} ", correlationId);
        String fromAccountId = mapFromAccountIdToStatus.keySet().iterator().next().replaceAll("\"", "");
        String status = mapFromAccountIdToStatus.get(fromAccountId);
        logger.info("Trying to find From-Account ID: {}", fromAccountId);
        accountComponentClient.findById(UUID.fromString(fromAccountId)).orElseThrow(() -> {
            logger.error("Account with such ID was not found : {}", fromAccountId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Account with such ID: " + fromAccountId + " was not found");
        });

        logger.info("Trying to find All Account Payments with Status: {}", status);
        List<PaymentDTO> paymentDTOS = paymentRepository.findAllByFromAccountId(UUID.fromString(fromAccountId))
                .stream()
                .filter(payment -> payment.getStatus().equals(status))
                .map(FilteredEntity -> {
                    logger.info("Payments was found and received to the Controller: {}", FilteredEntity);
                    return convertPaymentModelToDTO(FilteredEntity);
                })
                .toList();

        logger.info("Trying to create topic: get-payments-by-status-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<PaymentDTO>> responseTopic = new ProducerRecord<>(
                "get-payments-by-status-response", null, paymentDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOSKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-all-payments-by-to-account", groupId = "account-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void getAllAccountPaymentsByToAccount(UUID toAccountId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Trying to find To-Account with ID: {}", toAccountId);
        accountComponentClient.findById(toAccountId).orElseThrow(() -> {
            logger.error("Account with such ID was not found: {}", toAccountId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Account with such ID: " + toAccountId + " was not found");
        });

        logger.info("Trying to find All Payments To-Account with ID: {}", toAccountId);
        List<PaymentDTO> paymentDTOS = paymentRepository.findAllByToAccountId(toAccountId).stream()
                .map(PaymentEntity -> {
                    logger.info("Payments was found and received to the Controller: {}", PaymentEntity);
                    return convertPaymentModelToDTO(PaymentEntity);
                })
                .toList();

        logger.info("Trying to create topic: get-all-payments-by-to-account-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<PaymentDTO>> responseTopic = new ProducerRecord<>(
                "get-all-payments-by-to-account-response", null, paymentDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOSKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-all-payments-by-payment-type", groupId = "account-component",
            containerFactory = "mapObjectToObjectKafkaListenerFactory")
    public void getAllAccountPaymentsByPaymentType(Map<String, String> mapFromAccountIdToPaymentType, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: get-all-payments-by-payment-type with correlation id: {} ", correlationId);
        String fromAccountId = mapFromAccountIdToPaymentType.keySet().iterator().next().replaceAll("\"", "");
        String paymentType = mapFromAccountIdToPaymentType.get(fromAccountId);
        logger.info("Trying to find From-Account ID: {}", fromAccountId);
        accountComponentClient.findById(UUID.fromString(fromAccountId))
                .orElseThrow(() -> {
                    logger.error("Account with such ID was not found: {}", fromAccountId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Account with such ID was not found: " + fromAccountId);
                });

        logger.info("Trying to find All Account Payments with Type: {}", paymentType);
        List<PaymentDTO> paymentDTOS = paymentRepository.findAllByPaymentType(paymentType).stream()
                .map(PaymentEntity -> {
                    logger.info("Payments was found and received to the Controller: {}", PaymentEntity);
                    return convertPaymentModelToDTO(PaymentEntity);
                })
                .toList();

        logger.info("Trying to create topic: get-all-payments-by-payment-type-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<PaymentDTO>> responseTopic = new ProducerRecord<>(
                "get-all-payments-by-payment-type-response", null, paymentDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOSKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-all-from-account-payments-by-date-range", groupId = "account-component",
            containerFactory = "listOfObjectsKafkaListenerFactory")
    public void getAllFromAccountPaymentsByPaymentDateRange(List<Object> listOfFromAccountIdFromPaymentDateToPaymentDate,
                                                            @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: get-all-from-account-payments-by-date-range with correlation id: {} ", correlationId);
        UUID fromAccountId = (UUID) listOfFromAccountIdFromPaymentDateToPaymentDate.get(0);
        LocalDateTime fromPaymentDate = (LocalDateTime) listOfFromAccountIdFromPaymentDateToPaymentDate.get(1);
        LocalDateTime toPaymentDate = (LocalDateTime) listOfFromAccountIdFromPaymentDateToPaymentDate.get(2);
        logger.info("Trying to find From-Account ID: {}", fromAccountId);
        accountComponentClient.findById(fromAccountId).orElseThrow(() -> {
            logger.error("Account with such ID was not found: {}", fromAccountId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Account with such ID: " + fromAccountId + " was not found");
        });

        logger.info("Trying to find All Account Payments with Date Range: " +
                "{}-{}", fromPaymentDate, toPaymentDate);
        List<PaymentDTO> paymentDTOS = paymentRepository.findAllByFromAccountIdAndPaymentDateBetween(
                        fromAccountId, fromPaymentDate, toPaymentDate).stream()
                .map(PaymentEntity -> {
                    logger.info("Payments was found and received to the Controller: {}", PaymentEntity);
                    return convertPaymentModelToDTO(PaymentEntity);
                })
                .toList();

        logger.info("Trying to create topic: get-all-from-account-payments-by-date-range-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<PaymentDTO>> responseTopic = new ProducerRecord<>(
                "get-all-from-account-payments-by-date-range-response", null, paymentDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOSKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-all-to-account-payments-by-date-range", groupId = "account-component",
            containerFactory = "listOfObjectsKafkaListenerFactory")
    public void getAllToAccountPaymentsByPaymentDateRange(List<Object> listOfToAccountIdFromPaymentDateToPaymentDate,
                                                          @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        logger.info("Got request from kafka topic: get-all-to-account-payments-by-date-range with correlation id: {} ",
                correlationId);
        UUID toAccountId = (UUID) listOfToAccountIdFromPaymentDateToPaymentDate.get(0);
        LocalDateTime fromPaymentDate = (LocalDateTime) listOfToAccountIdFromPaymentDateToPaymentDate.get(1);
        LocalDateTime toPaymentDate = (LocalDateTime) listOfToAccountIdFromPaymentDateToPaymentDate.get(2);

        logger.info("Trying to find To-Account with ID: {}", toAccountId);
        accountComponentClient.findById(toAccountId).orElseThrow(() -> {
            logger.error("Account with such ID was not found: {}", toAccountId);
            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Account with such ID: " + toAccountId + " was NOT Found");
        });

        logger.info("Trying to find All To Account Payments with Date Range: " +
                "{}-{}", fromPaymentDate, toPaymentDate);
        List<PaymentDTO> paymentDTOS = paymentRepository.findAllByToAccountIdAndPaymentDateBetween(toAccountId,
                        fromPaymentDate, toPaymentDate).stream()
                .map(PaymentEntity -> {
                    logger.info("Payments was found and received to the Controller: {}", PaymentEntity);
                    return convertPaymentModelToDTO(PaymentEntity);
                })
                .toList();

        logger.info("Trying to create topic: get-all-to-account-payments-by-date-range-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<PaymentDTO>> responseTopic = new ProducerRecord<>(
                "get-all-to-account-payments-by-date-range-response", null, paymentDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOSKafkaTemplate.send(responseTopic);
        logger.info("Topic was created and allocated in kafka broker successfully: {}", responseTopic.value());
    }
}
