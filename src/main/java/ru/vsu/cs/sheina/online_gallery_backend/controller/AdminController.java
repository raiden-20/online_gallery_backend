package ru.vsu.cs.sheina.online_gallery_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.sheina.online_gallery_backend.dto.event.EventChangeDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.event.EventCreateDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.UUIDRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.service.AdminService;
import ru.vsu.cs.sheina.online_gallery_backend.service.KeycloakService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> blockUser(@RequestBody UUIDRequestDTO uuidRequestDTO) {
        adminService.blockUser(uuidRequestDTO);
        return ResponseEntity.ok("User blocked successfully");
    }

    @PostMapping("/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> unblockUser(@RequestBody UUIDRequestDTO uuidRequestDTO) {
        adminService.unblockUser(uuidRequestDTO);
        return ResponseEntity.ok("User unblocked successfully");
    }

    @DeleteMapping("/art")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteArt(@RequestBody IntIdRequestDTO intIdRequestDTO) {
        adminService.deleteArt(intIdRequestDTO);
        return ResponseEntity.ok("Art deleted successfully");
    }

    @DeleteMapping("/auction")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAuction(@RequestBody IntIdRequestDTO intIdRequestDTO) {
        adminService.deleteAuction(intIdRequestDTO);
        return ResponseEntity.ok("Auction deleted successfully");
    }

    @PostMapping(value = "/event", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE,
            MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createEvent(@RequestPart("EventCreateDTO") EventCreateDTO eventCreateDTO,
                                         @RequestPart(value = "photo") MultipartFile photo,
                                         @RequestPart(value = "banner") MultipartFile banner) {
        return ResponseEntity.ok(adminService.createEvent(eventCreateDTO, photo, banner));
    }

    @PutMapping(value = "/event", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE,
            MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeEvent(@RequestPart("EventChangeDTO") EventChangeDTO eventCreateDTO,
                                         @RequestPart(value = "newPhoto") MultipartFile newPhoto,
                                         @RequestPart(value = "newBanner") MultipartFile newBanner) {
        adminService.changeEvent(eventCreateDTO, newPhoto, newBanner);
        return ResponseEntity.ok("Event changed successfully");
    }

    @DeleteMapping("/event")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteEvent(@RequestBody IntIdRequestDTO intIdRequestDTO) {
        adminService.deleteEvent(intIdRequestDTO);
        return ResponseEntity.ok("Event deleted successfully");
    }
}
