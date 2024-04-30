package ru.vsu.cs.sheina.online_gallery_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.EmailDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.PasswordDTO;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentialsException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.EmailAlreadyExistsException;
import ru.vsu.cs.sheina.online_gallery_backend.service.CustomerService;
import ru.vsu.cs.sheina.online_gallery_backend.service.KeycloakService;

@RestController
@Tag(name = "Пользователь")
@RequiredArgsConstructor
public class UserController {

    private final KeycloakService keycloakService;
    private final CustomerService customerService;

    @PutMapping("/change/email")
    @Operation(summary = "Изменить электронную почту")
    @ApiResponse(responseCode = "200",
            description = "Проверьте свой электронный ящик",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "409",
            description = "Электронная почта уже занята",
            content = @Content(schema = @Schema(implementation = EmailAlreadyExistsException.class)))
    public ResponseEntity<?> changeEmail(@RequestBody EmailDTO emailDTO,
                                         @RequestHeader("Authorization") String token) {
        keycloakService.changeEmail(emailDTO, token);
        return ResponseEntity.ok("Check your new mailbox");
    }

    @PutMapping("/change/password")
    @Operation(summary = "Изменить пароль")
    @ApiResponse(responseCode = "200",
            description = "Пароль успешно изменен",
            content = @Content(schema = @Schema(implementation = String.class)))
    public ResponseEntity<?> changePassword(@RequestBody PasswordDTO passwordDTO,
                                            @RequestHeader("Authorization") String token){
        keycloakService.changePassword(passwordDTO, token);
        return ResponseEntity.ok("Data updated successfully");
    }


    @DeleteMapping("/account")
    @Operation(summary = "Удалить аккаунт")
    @ApiResponse(responseCode = "200",
            description = "Аккаунт успешно удален",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    public ResponseEntity<?> deleteAccount(@RequestHeader("Authorization") String token) {
        keycloakService.deleteAccount(token);
        customerService.deleteAccount(token);
        return ResponseEntity.ok("Account deleted successfully");
    }
}
