package com.example.accountcomponent.feign;

import com.example.accountcomponent.dto.CardDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class CardComponentClientFallback implements CardComponentClient{
    @Override
    public List<CardDTO> findAllCardsByAccountId(UUID accountId) {
        return Collections.emptyList();
    }

    @Override
    public void deleteAllAccountCardsByAccountId(UUID accountId) {

    }
}
