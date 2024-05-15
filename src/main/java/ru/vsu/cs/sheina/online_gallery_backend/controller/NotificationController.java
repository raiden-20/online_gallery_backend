package ru.vsu.cs.sheina.online_gallery_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import ru.vsu.cs.sheina.online_gallery_backend.service.NotificationService;
import ru.vsu.cs.sheina.online_gallery_backend.utils.JWTParser;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(path = "/notification/sse/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent> openSseStream(@RequestPart UUID id) {

        return Flux.create(fluxSink -> {
            fluxSink.onCancel(
                    () -> {
                        notificationService.deleteUserFromSubscriptions(id);
                    }

            );

            notificationService.addUserToSubscriptions(id, fluxSink);
//            ServerSentEvent<String> event = ServerSentEvent.builder().build();
//            fluxSink.next(event);
        });
    }

    @GetMapping("/artist")
    public ResponseEntity<?> getAllArtistNotification(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(notificationService.getArtistNotification(token));
    }

    @GetMapping("/customer")
    public ResponseEntity<?> getAllCustomerNotification(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(notificationService.getCustomerNotification(token));
    }
}