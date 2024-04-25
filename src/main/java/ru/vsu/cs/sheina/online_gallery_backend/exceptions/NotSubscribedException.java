package ru.vsu.cs.sheina.online_gallery_backend.exceptions;

import org.springframework.http.HttpStatus;

public class NotSubscribedException extends AppException{

    public NotSubscribedException() {
        super("Not subscribed to the artist", HttpStatus.FORBIDDEN);
    }
}
