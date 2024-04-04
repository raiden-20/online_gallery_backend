package ru.vsu.cs.sheina.online_gallery_backend.exceptions;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends AppException {
    public UserNotFoundException() {
        super("User's not found", HttpStatus.BAD_REQUEST);
    }
}
