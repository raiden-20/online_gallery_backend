package ru.vsu.cs.sheina.online_gallery_backend.exceptions;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends AppException{
    public EmailAlreadyExistsException() {
        super(" Email already exists", HttpStatus.CONFLICT);
    }
}

