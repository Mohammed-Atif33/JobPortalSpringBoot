package com.atif.jobportal.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidOAuthLoginCodeException extends ResponseStatusException {

    public InvalidOAuthLoginCodeException() {
        super(HttpStatus.UNAUTHORIZED, "OAuth login code is invalid or expired");
    }
}
