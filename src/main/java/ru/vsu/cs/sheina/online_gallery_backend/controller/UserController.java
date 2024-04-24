package ru.vsu.cs.sheina.online_gallery_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.DeleteDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.EmailDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.PasswordDTO;
import ru.vsu.cs.sheina.online_gallery_backend.service.CustomerService;
import ru.vsu.cs.sheina.online_gallery_backend.service.KeycloakService;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final KeycloakService keycloakService;
    private final CustomerService customerService;

    @PutMapping("/change/email")
    public ResponseEntity<?> changeEmail(@RequestBody EmailDTO emailDTO,
                                         @RequestHeader("Authorization") String token) {
        keycloakService.changeEmail(emailDTO, token);
        return ResponseEntity.ok("Check your new mailbox");
    }

    @PutMapping("/change/password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordDTO passwordDTO,
                                            @RequestHeader("Authorization") String token){
        keycloakService.changePassword(passwordDTO, token);
        return ResponseEntity.ok("Data updated successfully");
    }


    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount(@RequestHeader("Authorization") String token) {
        keycloakService.deleteAccount(token);
        customerService.deleteAccount(token);
        return ResponseEntity.ok("Account deleted successfully");
    }
}
