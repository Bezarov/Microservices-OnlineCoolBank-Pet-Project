package com.example.cardcomponent.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID accountId;
    private String cardNumber;
    private String cardHolderFullName;
    private UUID cardHolderUUID;
    private LocalDate expirationDate;
    private String cvv;
    private String status;

    public Card() {
    }

    public Card(UUID id, UUID accountId, String cardNumber, String cardHolderFullName, UUID cardHolderUUID,
                LocalDate expirationDate, String cvv, String status) {
        this.id = id;
        this.accountId = accountId;
        this.cardNumber = cardNumber;
        this.cardHolderFullName = cardHolderFullName;
        this.cardHolderUUID = cardHolderUUID;
        this.expirationDate = expirationDate;
        this.cvv = cvv;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
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

    public UUID getCardHolderUUID() {
        return cardHolderUUID;
    }

    public void setCardHolderUUID(UUID cardHolderUUID) {
        this.cardHolderUUID = cardHolderUUID;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", accountId=" + accountId +
                ", cardNumber='" + cardNumber + '\'' +
                ", cardHolderFullName='" + cardHolderFullName + '\'' +
                ", cardHolderUUID=" + cardHolderUUID +
                ", expirationDate=" + expirationDate +
                ", cvv='" + cvv + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
