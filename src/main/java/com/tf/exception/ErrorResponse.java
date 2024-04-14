package com.tf.exception;

import lombok.Data;

@Data
public class ErrorResponse {

    private String message;
    private String stackTrace;

    public ErrorResponse(String message) {
        this.message = message;
    }
}
