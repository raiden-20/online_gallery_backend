package ru.vsu.cs.sheina.online_gallery_backend.exceptions;

import org.springframework.http.HttpStatus;

public class BadActionException extends AppException {

    public BadActionException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
