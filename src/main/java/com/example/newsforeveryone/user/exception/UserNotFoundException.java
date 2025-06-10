package com.example.newsforeveryone.user.exception;

import com.example.newsforeveryone.common.exception.ErrorCode;

import java.util.Map;

public class UserNotFoundException extends UserException {

    public UserNotFoundException(Map<String, Object> details) {
        super(ErrorCode.USER_NOT_FOUND, details);
    }

}
