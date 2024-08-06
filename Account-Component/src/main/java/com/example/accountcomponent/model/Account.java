package com.example.accountcomponent.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String accountName;
    private String accountHolderFullName;
    private BigDecimal balance;
    private String accountType;
    private LocalDateTime createdDate;
    private String status;
    private String currency;

    public Account() {
    }

    public Account(UUID id, String accountName, String accountHolderFullName, BigDecimal balance,
                   String accountType, LocalDateTime createdDate, String status,
                   String currency) {
        this.id = id;
        this.accountName = accountName;
        this.accountHolderFullName = accountHolderFullName;
        this.balance = balance;
        this.accountType = accountType;
        this.createdDate = createdDate;
        this.status = status;
        this.currency = currency;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountHolderFullName() {
        return accountHolderFullName;
    }

    public void setAccountHolderFullName(String accountHolderName) {
        this.accountHolderFullName = accountHolderName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", accountName='" + accountName + '\'' +
                ", accountHolderFullName='" + accountHolderFullName + '\'' +
                ", balance=" + balance +
                ", currency='" + currency + '\'' +
                ", accountType='" + accountType + '\'' +
                ", createdDate=" + createdDate +
                ", status='" + status + '\'' +
                '}';
    }
}
