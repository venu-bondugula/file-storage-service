package com.tf.exception;

import org.springframework.http.HttpStatus;

public class FileNotProvidedException extends CustomException {
    public FileNotProvidedException() {
        super("No file was provided in the input");
        this.setErrorCode(HttpStatus.BAD_REQUEST);
    }

}
