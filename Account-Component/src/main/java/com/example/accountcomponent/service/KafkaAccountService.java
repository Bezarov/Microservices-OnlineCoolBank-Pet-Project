package com.example.accountcomponent.service;

import com.example.accountcomponent.dto.AccountDTO;
import com.example.accountcomponent.dto.RefillRequestDTO;
import com.example.accountcomponent.dto.UpdateRequestDTO;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public interface KafkaAccountService {

    void createAccount(Map<String, AccountDTO> userIdToAccountDTOMap, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getAccountByAccountName(String accountName, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getAccountById(UUID accountId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getAllUserAccountsByUserId(UUID userId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getAllAccountsByHolderFullName(String accountHolderFullName, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getBalanceByAccountId(UUID accountId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void getAllAccountsWithStatusByUserId(Map<String, String> userIdToAccountStatusMap, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void refillAccount(RefillRequestDTO refillRequestDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void updateAccountById(UpdateRequestDTO updateRequestDTO, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void updateAccountStatusById(Map<String, String> accountIdToStatusMap, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void updateAccountBalanceById(Map<String, BigDecimal> accountIdToNewBalanceMap, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void updateAccountBalanceByAccountName(Map<String, BigDecimal> accountNameToNewBalanceMap, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void deleteAccountByAccountId(UUID accountId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void deleteAccountByAccountName(String accountName, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);

    void deleteAllUserAccountsByUserId(UUID userId, @Header(KafkaHeaders.CORRELATION_ID) String correlationId);
}
