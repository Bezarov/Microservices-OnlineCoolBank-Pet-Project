package com.example.accountcomponent.client;

import com.example.accountcomponent.dto.CardDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@Qualifier("Card-Component")
@FeignClient(name = "Card-Component", url = "http://localhost:8100/card", fallback = CardComponentFallback.class)
public interface CardComponent {
    @GetMapping("/by-account-id/{accountId}")
    List<CardDTO> findAllCardsByAccountId(@PathVariable UUID accountId);
    @DeleteMapping("/by-account-id/{accountId}")
    void deleteAllAccountCardsByAccountId(@PathVariable UUID accountId);
}
