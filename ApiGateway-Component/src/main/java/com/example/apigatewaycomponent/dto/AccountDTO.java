package com.example.apigatewaycomponent.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AccountDTO {
    private UUID id;
    private String accountName;
    private BigDecimal balance;
    private String accountHolderFullName;
    private String status;
    private String accountType;
    private LocalDateTime createdDate;
    private String currency;
    private List<CardDTO> cards = null;

    public AccountDTO() {
    }

    public AccountDTO(UUID id, String accountName, BigDecimal balance, String accountHolderFullName,
                      String status, String accountType, LocalDateTime createdDate,
                      String currency, List<CardDTO> cards) {
        this.id = id;
        this.accountName = accountName;
        this.balance = balance;
        this.accountHolderFullName = accountHolderFullName;
        this.status = status;
        this.accountType = accountType;
        this.createdDate = createdDate;
        this.currency = currency;
        this.cards = cards;
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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getAccountHolderFullName() {
        return accountHolderFullName;
    }

    public void setAccountHolderFullName(String accountHolderFullName) {
        this.accountHolderFullName = accountHolderFullName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<CardDTO> getCards() {
        return cards;
    }

    public void setCards(List<CardDTO> cards) {
        this.cards = cards;
    }

    @Override
    public String toString() {
        return "AccountDTO{" +
                "id=" + id +
                ", accountName='" + accountName + '\'' +
                ", balance=" + balance +
                ", accountHolderFullName='" + accountHolderFullName + '\'' +
                ", status='" + status + '\'' +
                ", accountType='" + accountType + '\'' +
                ", createdDate=" + createdDate +
                ", currency='" + currency + '\'' +
                ", cards=" + cards +
                '}';
    }
}