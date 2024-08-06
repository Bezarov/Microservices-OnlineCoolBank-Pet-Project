package com.example.userscomponent.dto;

import java.time.LocalDate;
import java.util.UUID;

public class CardDTO {
    private UUID id;
    private UUID accountId;
    private UUID cardHolderId;
    private String cardNumber;
    private String cardHolderFullName;
    private LocalDate expirationDate;
    private String cvv;
    private String status;

    public CardDTO() {
    }

    public CardDTO(UUID id, String cardNumber, String cardHolderFullName,
                   UUID cardHolderId, LocalDate expirationDate, String cvv, UUID accountId, String status) {
        this.id = id;
        this.cardNumber = cardNumber;
        this.cardHolderFullName = cardHolderFullName;
        this.cardHolderId = cardHolderId;
        this.expirationDate = expirationDate;
        this.cvv = cvv;
        this.accountId = accountId;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCardHolderId() {
        return cardHolderId;
    }

    public void setCardHolderId(UUID cardHolderId) {
        this.cardHolderId = cardHolderId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolderFullName() {
        return cardHolderFullName;
    }

    public void setCardHolderFullName(String cardHolderFullName) {
        this.cardHolderFullName = cardHolderFullName;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}