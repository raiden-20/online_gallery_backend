package ru.vsu.cs.sheina.online_gallery_backend.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AppExceptionHandler {
    @ExceptionHandler
    public ResponseEntity<?> handleException(AppException appException) {
        return ResponseEntity.status(appException.getStatus()).body(appException.getMessage());
    }
}
