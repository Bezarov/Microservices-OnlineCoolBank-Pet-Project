package com.example.accountcomponent.repository;

import com.example.accountcomponent.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByAccountName(String accountName);

    List<Account> findAllByUsersId(UUID userId);

    List<Account> findByAccountHolderFullName(String accountHolderFullName);

}
