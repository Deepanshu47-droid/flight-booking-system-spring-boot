package com.checkin_service.exceptions;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(FeignException.class)
    public void handleFeignException(FeignException ex) {
        // Resolve status code from FeignException
        HttpStatus status = HttpStatus.resolve(ex.status());

        String message = ex.contentUTF8();

        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        throw new ResponseStatusException(
                status,
                message != null ? message : "Unknown error from downstream service"
        );
    }

}
