package com.harshitksinghai.CodeControl_Backend.Services;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

public interface LinkService {
    String generateLink();

    @Transactional
    void addLinkDetails(String email, String link);

    @Transactional
    ResponseEntity<String> verifyLink(String code);

    @Transactional
    void clearExpiredLinks();
}
