package com.musalasoft.ayoola.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ErrorControllerAdvice extends ResponseEntityExceptionHandler {

    /* Handle and format ResponseStatusExceptions
     *
     * @param ex thrown ResponseStatusException
     * @Param request Servlet Request
     *
     * @return ResponseEntity
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> customException(ResponseStatusException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", ex.getStatusCode());
        body.put("error", ex.getReason());
        body.put("message", ex.getMessage());
        body.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());
        body.put("timestamp", new Date());

        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    /* Handle and format Validation Errors
     *
     * @param ex thrown MethodArgumentNotValidException
     * @Param request Servlet Request
     *
     * @return ResponseEntity
     */
    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        Map<String, Object> body = new HashMap<>();
        body.put("status", ex.getStatusCode());
        body.put("error", "Validation Error");
        body.put("message", validationErrors);
        body.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());
        body.put("timestamp", new Date());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }
}
