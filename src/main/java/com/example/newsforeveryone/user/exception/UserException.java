package com.example.newsforeveryone.user.exception;

import com.example.newsforeveryone.common.exception.BaseException;
import com.example.newsforeveryone.common.exception.ErrorCode;

import java.util.Map;

public class UserException extends BaseException {

    public UserException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

}
