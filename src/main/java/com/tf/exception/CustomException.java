package com.tf.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private HttpStatus errorCode = HttpStatus.INTERNAL_SERVER_ERROR;

    public CustomException(String message) {
        super(message);
    }

    protected void setErrorCode(HttpStatus errorCode) {
        this.errorCode = errorCode;
    }
}
