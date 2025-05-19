package com.example.paymentcomponent.service;

import com.example.paymentcomponent.dto.AccountDTO;
import com.example.paymentcomponent.dto.DateRangeRequestDTO;
import com.example.paymentcomponent.dto.PaymentDTO;
import com.example.paymentcomponent.exception.CustomKafkaException;
import com.example.paymentcomponent.feign.AccountComponentClient;
import com.example.paymentcomponent.feign.CardComponentClient;
import com.example.paymentcomponent.model.Payment;
import com.example.paymentcomponent.repository.PaymentRepository;
import jakarta.transaction.Transactional;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class KafkaPaymentServiceImpl implements KafkaPaymentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPaymentServiceImpl.class);
    private static final String ALLOCATED_TOPIC_LOG = "Topic was created and allocated in kafka broker successfully: {}";
    private static final String ACCOUNT_FROM_SEARCHING_LOG = "Trying to find From-Account by: {}";
    private static final String ACCOUNT_TO_SEARCHING_LOG = "Trying to find To-Account by: {}";
    private static final String ACCOUNT_FROM_NOT_FOUND_LOG = "From-Account was not found by: {}";
    private static final String ACCOUNT_TO_NOT_FOUND_LOG = "To-Account was not found by: {}";
    private static final String PAYMENT_WAS_FOUND_LOG = "Payments was found in DB: {}";
    private static final String CARD_FROM_NOT_FOUND_LOG = "From-Card was not found by: {}";
    private static final String CARD_TO_NOT_FOUND_LOG = "To-Card was not found by: {}";
    private static final String PAYMENT_SEARCHING_LOG = "Trying to find Payment by: {}";
    private static final String PAYMENT_NOT_FOUND_LOG = "Payment was not found by: {}";

    private final KafkaTemplate<String, PaymentDTO> responseDTOKafkaTemplate;
    private final KafkaTemplate<String, AccountDTO> requestDTOKafkaTemplate;
    private final KafkaTemplate<String, List<PaymentDTO>> responseDTOSKafkaTemplate;

    private final AccountComponentClient accountComponentClient;
    private final CardComponentClient cardComponentClient;
    private final PaymentRepository paymentRepository;

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

    private PaymentDTO buildCardToCardPaymentDTO(UUID fromAccountId, UUID toAccountId, BigDecimal amount,
                                                 String fromCardNumber, String toCardNumber) {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setFromAccount(fromAccountId);
        paymentDTO.setToAccount(toAccountId);
        paymentDTO.setAmount(amount);
        paymentDTO.setPaymentType("Card to Card Transfer");
        paymentDTO.setDescription("Transfer from Card: " + fromCardNumber + " to Card: " + toCardNumber);
        return paymentDTO;
    }

    @Override
    @KafkaListener(topics = "create-payment-by-accounts", groupId = "payment-component",
            containerFactory = "paymentDTOKafkaListenerFactory")
    public void createPaymentByAccounts(PaymentDTO paymentDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: create-payment-by-accounts with correlation id: {} ", correlationId);

        AccountDTO fromAccount = findAccountOrThrow(paymentDTO.getFromAccount(), true);
        AccountDTO toAccount = findAccountOrThrow(paymentDTO.getToAccount(), false);

        checkSufficientFunds(fromAccount, paymentDTO.getAmount());

        debitFunds(fromAccount, paymentDTO.getAmount(), correlationId);
        creditFunds(toAccount, paymentDTO.getAmount(), correlationId);

        Payment savedPayment = savePayment(paymentDTO, fromAccount.getId(), toAccount.getId());

        sendPaymentResponse(savedPayment, correlationId);
    }

    @Override
    @KafkaListener(topics = "create-payment-by-cards", groupId = "payment-component",
            containerFactory = "paymentListKafkaListenerFactory")
    public void createPaymentByCards(String fromCardNumber, String toCardNumber, BigDecimal amount,
                                     @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: create-payment-by-cards with correlation id: {}", correlationId);

        AccountDTO fromAccount = getAccountByCardNumber(fromCardNumber, true);
        AccountDTO toAccount = getAccountByCardNumber(toCardNumber, false);

        checkSufficientFunds(fromAccount, amount);

        debitFunds(fromAccount, amount, correlationId);
        creditFunds(toAccount, amount, correlationId);

        Payment savedPayment = savePayment(buildCardToCardPaymentDTO(fromAccount.getId(), toAccount.getId(), amount, fromCardNumber, toCardNumber),
                fromAccount.getId(), toAccount.getId());

        sendPaymentResponse(savedPayment, correlationId);
    }

    @Override
    @KafkaListener(topics = "get-payment-by-id", groupId = "payment-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void getPaymentById(UUID paymentId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-payment-by-id with correlation id: {} ", correlationId);
        LOGGER.info(PAYMENT_SEARCHING_LOG, paymentId);

        PaymentDTO paymentDTO = findPaymentDTOById(paymentId);
        LOGGER.info(PAYMENT_WAS_FOUND_LOG, paymentDTO);

        LOGGER.info("Trying to create topic: get-payment-by-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, PaymentDTO> responseTopic = new ProducerRecord<>(
                "get-payment-by-id-response", null, paymentDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-all-payments-by-from-account-id", groupId = "payment-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void getAllAccountPaymentsByFromAccount(UUID fromAccountId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-all-payments-by-from-account-id with correlation id: {} ", correlationId);
        LOGGER.info(ACCOUNT_FROM_SEARCHING_LOG, fromAccountId);

        validateAccountExistenceByAccountId(fromAccountId);

        LOGGER.info("Trying to find all Account Payments with Account ID: {}", fromAccountId);
        List<PaymentDTO> paymentDTOS = paymentRepository.findAllByFromAccountId(fromAccountId).stream()
                .map(this::convertPaymentModelToDTO)
                .toList();
        LOGGER.info(PAYMENT_WAS_FOUND_LOG, paymentDTOS);

        LOGGER.info("Trying to create topic: get-all-payment-by-from-account-id-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<PaymentDTO>> responseTopic = new ProducerRecord<>(
                "get-all-payment-by-from-account-id-response", null, paymentDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOSKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-payments-by-status", groupId = "payment-component",
            containerFactory = "mapObjectToObjectKafkaListenerFactory")
    public void getPaymentsByStatus(Map<String, String> mapFromAccountIdToStatus, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-payments-by-status with correlation id: {} ", correlationId);
        String fromAccountId = mapFromAccountIdToStatus.keySet().iterator().next().replaceAll("\"", "");
        String status = mapFromAccountIdToStatus.get(fromAccountId);
        LOGGER.info(ACCOUNT_FROM_SEARCHING_LOG, fromAccountId);

        validateAccountExistenceByAccountId(UUID.fromString(fromAccountId));

        LOGGER.info("Trying to find All Account Payments with Status: {}", status);
        List<PaymentDTO> paymentDTOS = paymentRepository.findAllByFromAccountId(UUID.fromString(fromAccountId))
                .stream()
                .filter(payment -> payment.getStatus().equals(status))
                .map(filteredEntity -> {
                    LOGGER.info(PAYMENT_WAS_FOUND_LOG, filteredEntity);
                    return convertPaymentModelToDTO(filteredEntity);
                })
                .toList();

        LOGGER.info("Trying to create topic: get-payments-by-status-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<PaymentDTO>> responseTopic = new ProducerRecord<>(
                "get-payments-by-status-response", null, paymentDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOSKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-all-payments-by-to-account", groupId = "payment-component",
            containerFactory = "uuidKafkaListenerFactory")
    public void getAllAccountPaymentsByToAccount(UUID toAccountId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info(ACCOUNT_TO_SEARCHING_LOG, toAccountId);

        validateAccountExistenceByAccountId(toAccountId);

        LOGGER.info("Trying to find All Payments To-Account with ID: {}", toAccountId);
        List<PaymentDTO> paymentDTOS = paymentRepository.findAllByToAccountId(toAccountId).stream()
                .map(paymentEntity -> {
                    LOGGER.info(PAYMENT_WAS_FOUND_LOG, paymentEntity);
                    return convertPaymentModelToDTO(paymentEntity);
                })
                .toList();

        LOGGER.info("Trying to create topic: get-all-payments-by-to-account-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<PaymentDTO>> responseTopic = new ProducerRecord<>(
                "get-all-payments-by-to-account-response", null, paymentDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOSKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-all-payments-by-payment-type", groupId = "payment-component",
            containerFactory = "mapObjectToObjectKafkaListenerFactory")
    public void getAllAccountPaymentsByPaymentType(Map<String, String> mapFromAccountIdToPaymentType, @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-all-payments-by-payment-type with correlation id: {} ", correlationId);
        String fromAccountId = mapFromAccountIdToPaymentType.keySet().iterator().next().replaceAll("\"", "");
        String paymentType = mapFromAccountIdToPaymentType.get(fromAccountId);
        LOGGER.info(ACCOUNT_FROM_SEARCHING_LOG, fromAccountId);

        validateAccountExistenceByAccountId(UUID.fromString(fromAccountId));

        LOGGER.info("Trying to find All Account Payments with Type: {}", paymentType);
        List<PaymentDTO> paymentDTOS = paymentRepository.findAllByPaymentType(paymentType).stream()
                .map(paymentEntity -> {
                    LOGGER.info(PAYMENT_WAS_FOUND_LOG, paymentEntity);
                    return convertPaymentModelToDTO(paymentEntity);
                })
                .toList();

        LOGGER.info("Trying to create topic: get-all-payments-by-payment-type-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<PaymentDTO>> responseTopic = new ProducerRecord<>(
                "get-all-payments-by-payment-type-response", null, paymentDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOSKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-all-from-account-payments-by-date-range", groupId = "payment-component",
            containerFactory = "paymentListKafkaListenerFactory")
    public void getAllFromAccountPaymentsByPaymentDateRange(DateRangeRequestDTO requestDTO,
                                                            @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-all-from-account-payments-by-date-range with correlation id: {} ", correlationId);
        LOGGER.info(ACCOUNT_FROM_SEARCHING_LOG, requestDTO.accountId());

        validateAccountExistenceByAccountId(requestDTO.accountId());

        LOGGER.info("Trying to find All Account Payments with Date Range: " +
                "{}-{}", requestDTO.fromDate(), requestDTO.toDate());
        List<PaymentDTO> paymentDTOS = paymentRepository.findAllByFromAccountIdAndPaymentDateBetween(
                        requestDTO.accountId(), requestDTO.fromDate(), requestDTO.toDate())
                .stream()
                .map(paymentEntity -> {
                    LOGGER.info(PAYMENT_WAS_FOUND_LOG, paymentEntity);
                    return convertPaymentModelToDTO(paymentEntity);
                })
                .toList();

        LOGGER.info("Trying to create topic: get-all-from-account-payments-by-date-range-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<PaymentDTO>> responseTopic = new ProducerRecord<>(
                "get-all-from-account-payments-by-date-range-response", null, paymentDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOSKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Override
    @KafkaListener(topics = "get-all-to-account-payments-by-date-range", groupId = "payment-component",
            containerFactory = "paymentListKafkaListenerFactory")
    public void getAllToAccountPaymentsByPaymentDateRange(DateRangeRequestDTO requestDTO,
                                                          @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        LOGGER.info("Got request from kafka topic: get-all-to-account-payments-by-date-range with correlation id: {} ", correlationId);
        LOGGER.info(ACCOUNT_TO_SEARCHING_LOG, requestDTO.accountId());

        validateAccountExistenceByAccountId(requestDTO.accountId());

        LOGGER.info("Trying to find All To Account Payments with Date Range: " + "{}-{}", requestDTO.fromDate(), requestDTO.toDate());
        List<PaymentDTO> paymentDTOS = paymentRepository.findAllByToAccountIdAndPaymentDateBetween(requestDTO.accountId(),
                        requestDTO.fromDate(), requestDTO.toDate())
                .stream()
                .map(paymentEntity -> {
                    LOGGER.info(PAYMENT_WAS_FOUND_LOG, paymentEntity);
                    return convertPaymentModelToDTO(paymentEntity);
                })
                .toList();

        LOGGER.info("Trying to create topic: get-all-to-account-payments-by-date-range-response with correlation id: {} ", correlationId);
        ProducerRecord<String, List<PaymentDTO>> responseTopic = new ProducerRecord<>(
                "get-all-to-account-payments-by-date-range-response", null, paymentDTOS);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOSKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    private AccountDTO getAccountByCardNumber(String cardNumber, boolean isFrom) {
        LOGGER.info("{} {}", isFrom ? ACCOUNT_FROM_SEARCHING_LOG : ACCOUNT_TO_SEARCHING_LOG, cardNumber);

        return cardComponentClient.findByCardNumber(cardNumber)
                .map(cardDTO -> {
                    LOGGER.info("{}-Card was found successfully with card number: {}", isFrom ? "From" : "To", cardDTO);
                    LOGGER.info("Trying to find Linked Account to this Card");
                    return accountComponentClient.findById(cardDTO.getAccountId())
                            .orElseThrow(() -> {
                                LOGGER.error("Linked Account to this Card number was not found: {}", cardNumber);
                                return new CustomKafkaException(HttpStatus.NOT_FOUND,
                                        "Linked Account to this Card Number: " + cardNumber + " was not found");
                            });
                })
                .orElseThrow(() -> {
                    LOGGER.error(isFrom ? CARD_FROM_NOT_FOUND_LOG : CARD_TO_NOT_FOUND_LOG, cardNumber);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Card with such ID: " + cardNumber + " was not found");
                });
    }

    private PaymentDTO findPaymentDTOById(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .map(this::convertPaymentModelToDTO)
                .orElseThrow(() -> {
                    LOGGER.error(PAYMENT_NOT_FOUND_LOG, paymentId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Payment with such ID was NOT Found: " + paymentId);
                });
    }

    private void validateAccountExistenceByAccountId(UUID accountId) {
        accountComponentClient.findById(accountId)
                .orElseThrow(() -> {
                    LOGGER.error(ACCOUNT_FROM_NOT_FOUND_LOG, accountId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            "Account with such ID: " + accountId + " was not found");
                });
    }

    private AccountDTO findAccountOrThrow(UUID accountId, boolean isFrom) {
        LOGGER.info("{} {}", isFrom ? ACCOUNT_FROM_SEARCHING_LOG : ACCOUNT_TO_SEARCHING_LOG, accountId);
        return accountComponentClient.findById(accountId)
                .orElseThrow(() -> {
                    LOGGER.error(isFrom ? ACCOUNT_FROM_NOT_FOUND_LOG : ACCOUNT_TO_NOT_FOUND_LOG, accountId);
                    return new CustomKafkaException(HttpStatus.NOT_FOUND,
                            (isFrom ? "From" : "To") + "-Account ID: " + accountId + " was not found");
                });
    }

    private void checkSufficientFunds(AccountDTO fromAccount, BigDecimal amount) {
        LOGGER.info("Checking availability of sufficient funds From-Account ID: {}", fromAccount.getId());
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            LOGGER.error("Insufficient FUNDS for From-Account ID: {}", fromAccount.getId());
            throw new CustomKafkaException(HttpStatus.BAD_REQUEST, "INSUFFICIENT FUNDS");
        }
    }

    private void debitFunds(AccountDTO account, BigDecimal amount, String correlationId) {
        account.setBalance(account.getBalance().subtract(amount));
        LOGGER.info("Debiting account ID: {}", account.getId());
        sendAccountUpdate("debit-funds", account, correlationId);
    }

    private void creditFunds(AccountDTO account, BigDecimal amount, String correlationId) {
        account.setBalance(account.getBalance().add(amount));
        LOGGER.info("Crediting account ID: {}", account.getId());
        sendAccountUpdate("credit-funds", account, correlationId);
    }

    private void sendAccountUpdate(String topic, AccountDTO accountDTO, String correlationId) {
        LOGGER.info("Trying to create topic: {} with correlation id: {}", topic, correlationId);
        ProducerRecord<String, AccountDTO> responseTopic = new ProducerRecord<>(topic, null, accountDTO);
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        requestDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }

    @Transactional
    private Payment savePayment(PaymentDTO dto, UUID fromId, UUID toId) {
        dto.setPaymentType("Account to Account Transfer");
        LOGGER.info("Saving payment in DB");
        return paymentRepository.save(convertPaymentDTOToModel(fromId, toId, dto));
    }

    private void sendPaymentResponse(Payment payment, String correlationId) {
        LOGGER.info("Sending response to topic: create-payment-by-accounts-response with correlation id: {}", correlationId);
        ProducerRecord<String, PaymentDTO> responseTopic = new ProducerRecord<>(
                "create-payment-by-accounts-response", null, convertPaymentModelToDTO(payment));
        responseTopic.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes());
        responseDTOKafkaTemplate.send(responseTopic);
        LOGGER.info(ALLOCATED_TOPIC_LOG, responseTopic.value());
    }
}
