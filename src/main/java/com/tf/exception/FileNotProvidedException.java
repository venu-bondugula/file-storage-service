package com.tf.exception;

import org.springframework.http.HttpStatus;

public class FileNotProvidedException extends CustomException {
    public FileNotProvidedException() {
        super("No file is provided in the input", HttpStatus.BAD_REQUEST);
    }
}
