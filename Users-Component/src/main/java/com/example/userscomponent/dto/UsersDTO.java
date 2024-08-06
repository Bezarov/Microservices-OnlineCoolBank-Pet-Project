package com.example.userscomponent.dto;

import java.util.List;
import java.util.UUID;

public class UsersDTO {
    private UUID id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String password;
    private List<Object> account = null;

    public UsersDTO() {
    }

    public UsersDTO(UUID id, String fullName, String email, String phoneNumber,
                    String password, List<Object> account) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.account = account;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Object> getAccount() {
        return account;
    }

    public void setAccount(List<Object> account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", password='" + password + '\'' +
                ", account=" + account;
    }
}