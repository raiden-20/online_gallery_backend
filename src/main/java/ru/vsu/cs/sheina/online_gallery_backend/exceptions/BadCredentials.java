package ru.vsu.cs.sheina.online_gallery_backend.exceptions;

import org.springframework.http.HttpStatus;

public class BadCredentials extends AppException{
    public BadCredentials() {
        super("Bad credentials", HttpStatus.BAD_REQUEST);
    }
}
