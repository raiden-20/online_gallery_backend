package ru.vsu.cs.sheina.online_gallery_backend.exceptions;

import org.springframework.http.HttpStatus;

public class BlockUserException extends AppException{
    public BlockUserException() {
        super("User blocked", HttpStatus.NOT_FOUND);
    }
}
