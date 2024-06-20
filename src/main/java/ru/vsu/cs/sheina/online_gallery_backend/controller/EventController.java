package ru.vsu.cs.sheina.online_gallery_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vsu.cs.sheina.online_gallery_backend.dto.event.EventFullDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.event.EventShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.service.EventService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EventController {

    private final EventService eventService;

    @GetMapping("/event/eventId={eventId}&currentId={currentId}")
    public ResponseEntity<?> getEvent(@PathVariable Integer eventId,
                                      @PathVariable String currentId) {
        EventFullDTO eventFullDTO = eventService.getEvent(eventId, currentId);
        return ResponseEntity.ok(eventFullDTO);
    }

    @GetMapping("/events")
    public ResponseEntity<?> getEvents() {
        List<EventShortDTO> events = eventService.getEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/search/event/object={input}")
    public ResponseEntity<?> searchEvents(@PathVariable String input){
        List<EventShortDTO> events = eventService.searchEvents(input);
        return ResponseEntity.ok(events);
    }
}
