package ru.vsu.cs.sheina.online_gallery_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.EmailDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.PasswordDTO;
import ru.vsu.cs.sheina.online_gallery_backend.service.KeycloakService;

@RestController
@RequestMapping("/change")
@CrossOrigin
@RequiredArgsConstructor
public class UserController {

    private final KeycloakService keycloakService;

    @PutMapping("/email")
    public ResponseEntity<?> changeEmail(@RequestBody EmailDTO emailDTO) {
        keycloakService.changeEmail(emailDTO);
        return ResponseEntity.ok("Check your new mailbox");
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordDTO passwordDTO){
        keycloakService.changePassword(passwordDTO);
        return ResponseEntity.ok("Data updated successfully");
    }
}
