package ru.vsu.cs.sheina.online_gallery_backend.exceptions;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends AppException {
    public UserAlreadyExistsException() {
        super(" You already have an account", HttpStatus.CONFLICT);
    }
}
