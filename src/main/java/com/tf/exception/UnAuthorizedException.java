package com.tf.exception;

import org.springframework.http.HttpStatus;

public class UnAuthorizedException extends CustomException {
    public UnAuthorizedException() {
        super("You are not authorized to access this file", HttpStatus.UNAUTHORIZED);
    }
}
