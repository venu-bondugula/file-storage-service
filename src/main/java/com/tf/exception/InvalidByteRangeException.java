package com.tf.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;


public class InvalidByteRangeException extends CustomException {
    public InvalidByteRangeException(String errorUploadingFile, long fileSize) {
        super(errorUploadingFile, HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize);
        this.setHeaders(httpHeaders);
    }
}
