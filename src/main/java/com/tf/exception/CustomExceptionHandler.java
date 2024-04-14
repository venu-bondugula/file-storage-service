package com.tf.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

@Log4j2
@ControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleException(CustomException ex) {
        log.error("Exception occurred:", ex);
        boolean isDevEnv = Arrays.asList("local", "dev").contains(getActiveProfile());

        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        if (isDevEnv) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw, true);
            ex.printStackTrace(pw);
            errorResponse.setStackTrace(sw.getBuffer().toString());
        }
        if (ex.getHeaders() != null) {
            return new ResponseEntity<>(errorResponse, ex.getHeaders(), ex.getErrorCode());
        }
        return new ResponseEntity<>(errorResponse, ex.getErrorCode());
    }

    private String getActiveProfile() {
        return "local";
    }
}
