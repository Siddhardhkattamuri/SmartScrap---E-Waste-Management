package com.example.SmartScrap.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // This will catch any unexpected error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> globleExcpetionHandler(Exception ex, WebRequest request) {
        System.err.println(">>>>>> An unexpected error occurred: " + ex.getMessage());
        ex.printStackTrace(); // This prints the full error to your backend console

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("message", "An internal server error occurred. Please check the backend logs for details.");
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}