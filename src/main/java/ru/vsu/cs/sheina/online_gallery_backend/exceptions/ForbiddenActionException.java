package ru.vsu.cs.sheina.online_gallery_backend.exceptions;

import org.springframework.http.HttpStatus;

public class ForbiddenActionException extends AppException{
    public ForbiddenActionException() {
        super("Forbidden action", HttpStatus.FORBIDDEN);
    }
}
