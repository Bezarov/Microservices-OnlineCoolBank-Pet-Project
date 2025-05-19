package com.example.accountcomponent.repository;

import com.example.accountcomponent.model.Account;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByAccountName(String accountName);

    List<Account> findByAccountHolderFullName(String accountHolderFullName);

    @Query("SELECT account.balance FROM Account AS account WHERE account.id = :accountId")
    Optional<BigDecimal> findAccountBalanceById(@Param("accountId") UUID accountId);

    boolean existsById(@NonNull UUID accountId);
}
