package com.example.registerapp.custumeRuntimeException;

import com.example.registerapp.custumeRuntimeException.CustomRuntimeException;
import com.example.registerapp.custumeRuntimeException.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomRuntimeException.class)
    public ResponseEntity<ErrorResponse> handleCustomRuntimeException(CustomRuntimeException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

}

