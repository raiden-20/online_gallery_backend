package ru.vsu.cs.sheina.online_gallery_backend.exceptions;

import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AppExceptionHandler {
    @ExceptionHandler
    public ResponseEntity<?> handleException(AppException appException) {
        return ResponseEntity.status(appException.getStatus()).body(appException.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<?> handleException(InvalidJwtException invalidJwtException) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bad token");
    }

    @ExceptionHandler
    public ResponseEntity<?> handleException(MalformedClaimException malformedClaimException) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bad token");
    }
}
