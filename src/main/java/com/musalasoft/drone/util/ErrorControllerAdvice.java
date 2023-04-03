package com.musalasoft.drone.util;

import com.musalasoft.drone.util.exceptions.DroneRuntimeException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
    private static final String ERROR = "error";
    private static final String STATUS = "status";
    private static final String MESSAGE = "message";
    private static final String PATH = "path";
    private static final String TIMESTAMP = "timestamp";

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
        body.put(STATUS, ex.getStatusCode());
        body.put(ERROR, ex.getReason());
        body.put(MESSAGE, ex.getMessage());
        body.put(PATH, ((ServletWebRequest) request).getRequest().getRequestURI());
        body.put(TIMESTAMP, new Date());

        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    /* Handle and format ResponseStatusExceptions
     *
     * @param ex thrown ResponseStatusException
     * @Param request Servlet Request
     *
     * @return ResponseEntity
     */
    @ExceptionHandler(DroneRuntimeException.class)
    public ResponseEntity<Map<String, Object>> droneRuntimeExceptions(DroneRuntimeException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put(STATUS, HttpStatus.BAD_REQUEST);
        body.put(ERROR, "Unprocessable Action on Drones");
        body.put(MESSAGE, ex.getMessage());
        body.put(PATH, ((ServletWebRequest) request).getRequest().getRequestURI());
        body.put(TIMESTAMP, new Date());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
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
        body.put(STATUS, ex.getStatusCode());
        body.put(ERROR, "Validation Error");
        body.put(MESSAGE, validationErrors);
        body.put(PATH, ((ServletWebRequest) request).getRequest().getRequestURI());
        body.put(TIMESTAMP, new Date());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }
}
