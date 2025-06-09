package com.example.newsforeveryone.interest.exception;

import com.example.newsforeveryone.common.exception.ErrorCode;

import java.util.Map;

public class InterestAlreadyExistException extends InterestException {

    public InterestAlreadyExistException(Map<String, Object> details) {
        super(ErrorCode.INTEREST_AlREADY_EXIST, details);
    }

}
