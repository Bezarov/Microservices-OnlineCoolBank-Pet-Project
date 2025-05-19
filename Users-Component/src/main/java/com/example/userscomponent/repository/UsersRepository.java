package com.example.userscomponent.repository;

import com.example.userscomponent.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends JpaRepository<Users, UUID> {
    Optional<Users> findByEmail(String email);

    Optional<Users> findByFullNameIgnoreCase(String fullName);

    Optional<Users> findByPhoneNumber(String phoneNumber);

    void deleteByEmail(String email);

    void deleteByFullName(String userFullName);
}
