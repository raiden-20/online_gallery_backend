package ru.vsu.cs.sheina.online_gallery_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.sheina.online_gallery_backend.dto.address.AddressDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.address.AddressNewDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadActionException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentialsException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.ForbiddenActionException;
import ru.vsu.cs.sheina.online_gallery_backend.service.AddressService;

import java.util.List;

@RestController
@Tag(name = "Адрес")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/addresses")
    @Operation(summary = "Получить все сохраненные адреса пользователя")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список адресов",
            content = @Content(schema = @Schema(implementation = AddressDTO.class)))
    public ResponseEntity<?> getAddresses(@RequestHeader("Authorization") String token) {
        List<AddressDTO> addresses = addressService.getAddresses(token);
        return ResponseEntity.ok(addresses);
    }

    @PostMapping("/address")
    @Operation(summary = "Добавить новый адрес")
    @ApiResponse(responseCode = "200",
            description = "Адрес успешно добавлен",
            content = @Content(schema = @Schema(implementation = String.class)))
    public ResponseEntity<?> addNewAddress(@RequestBody AddressNewDTO addressNewDTO,
                                           @RequestHeader("Authorization") String token) {
        addressService.addNewAddress(addressNewDTO, token);
        return ResponseEntity.ok("Address added successfully");
    }

    @PutMapping("/address")
    @Operation(summary = "Изменить существующий адрес")
    @ApiResponse(responseCode = "200",
            description = "Адрес успешно изменен",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    public ResponseEntity<?> changeAddress(@RequestBody AddressDTO addressDTO,
                                           @RequestHeader("Authorization") String token) {
        addressService.changeAddress(addressDTO, token);
        return ResponseEntity.ok("Address changed successfully");
    }

    @DeleteMapping("/address")
    @Operation(summary = "Удалить существующий адрес")
    @ApiResponse(responseCode = "200",
            description = "Адрес успешно удален",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    @ApiResponse(responseCode = "409",
            description = "Адрес используется в активном заказе и не может быть удален",
            content = @Content(schema = @Schema(implementation = BadActionException.class)))
    @ApiResponse(responseCode = "403",
            description = "Действие запрещено",
            content = @Content(schema = @Schema(implementation = ForbiddenActionException.class)))
    public ResponseEntity<?> deleteAddress(@RequestBody IntIdRequestDTO intIdRequestDTO,
                                           @RequestHeader("Authorization") String token) {
        addressService.deleteAddress(intIdRequestDTO, token);
        return ResponseEntity.ok("Address deleted successfully");
    }
}
