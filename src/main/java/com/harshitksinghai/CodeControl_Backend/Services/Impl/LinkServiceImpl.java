package com.harshitksinghai.CodeControl_Backend.Services.Impl;

import com.harshitksinghai.CodeControl_Backend.Models.LinkVerification;
import com.harshitksinghai.CodeControl_Backend.Repositories.LinkVerificationRepository;
import com.harshitksinghai.CodeControl_Backend.Services.LinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class LinkServiceImpl implements LinkService {
    private final Logger LOG = LoggerFactory.getLogger(LinkServiceImpl.class);

    @Autowired
    LinkVerificationRepository linkVerificationRepository;

    @Override
    public String generateLink() {
        LOG.info("inside generateLink in LinkServiceImpl");
        return UUID.randomUUID().toString();
    }

    @Override
    public void addLinkDetails(String email, String code) {
        LOG.info("inside addLinkDetails in LinkServiceImpl");
        LinkVerification linkVerification = new LinkVerification();
        linkVerification.setEmail(email);
        linkVerification.setCode(code);
        linkVerification.setExpirationTime(LocalDateTime.now().plusMinutes(5));

        linkVerificationRepository.save(linkVerification);
    }

    @Override
    public ResponseEntity<String> verifyLink(String code) {
        LOG.info("inside verifyLink in LinkServiceImpl");
        Optional<LinkVerification> linkVerificationOpt = linkVerificationRepository.findByCode(code);
        if(linkVerificationOpt.isEmpty()){
            throw new IllegalArgumentException("magic-link not found");
        }

        LinkVerification linkVerification = linkVerificationOpt.get();
        String email = linkVerification.getEmail();

        if(linkVerification.getExpirationTime().isAfter(LocalDateTime.now())){
            linkVerificationRepository.deleteByCode(code);
            return new ResponseEntity<>(email, HttpStatus.OK);
        }
        else{
            linkVerificationRepository.deleteByCode(code);
            throw new IllegalArgumentException("Invalid or expired link.");
        }

    }

    @Override
    public void clearExpiredLinks() {
        LOG.info("inside clearExpiredLinks in LinkServiceImpl");
        LocalDateTime now = LocalDateTime.now();
        linkVerificationRepository.deleteByExpirationTimeBefore(now);
    }
}

