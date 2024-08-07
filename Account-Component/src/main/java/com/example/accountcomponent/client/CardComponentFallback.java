package com.example.accountcomponent.client;

import com.example.accountcomponent.dto.CardDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CardComponentFallback implements CardComponent{
    @Override
    public List<CardDTO> findAllCardsByAccountId(UUID accountId) {
        return null;
    }

    @Override
    public void deleteAllAccountCardsByAccountId(UUID accountId) {

    }
}
