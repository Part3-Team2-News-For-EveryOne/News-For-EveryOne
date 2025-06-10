package com.example.newsforeveryone.interest.exception;

import java.util.Map;

import static com.example.newsforeveryone.common.exception.ErrorCode.INTEREST_NOT_FOUND;

public class InterestNotFoundException extends InterestException {

    public InterestNotFoundException(Map<String, Object> details) {
        super(INTEREST_NOT_FOUND, details);
    }

}
