package ru.vsu.cs.sheina.online_gallery_backend.exceptions;

import org.springframework.http.HttpStatus;

public class BadCredentialsException extends AppException{
    public BadCredentialsException() {
        super("Bad credentials", HttpStatus.BAD_REQUEST);
    }
}
