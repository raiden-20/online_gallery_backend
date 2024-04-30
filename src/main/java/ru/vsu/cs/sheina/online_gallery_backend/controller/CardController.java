package ru.vsu.cs.sheina.online_gallery_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.sheina.online_gallery_backend.dto.card.CardDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.card.CardNewDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadActionException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentialsException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.ForbiddenActionException;
import ru.vsu.cs.sheina.online_gallery_backend.service.CardService;

import java.util.List;

@RestController
@Tag(name = "Банковская карта")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping("/cards")
    @Operation(summary = "Получить все сохраненные карты пользователя")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список карт",
            content = @Content(schema = @Schema(implementation = CardDTO.class)))
    public ResponseEntity<?> getCards(@RequestHeader("Authorization") String token) {
        List<CardDTO> cards = cardService.getCards(token);
        return ResponseEntity.ok(cards);
    }

    @PostMapping("/card")
    @Operation(summary = "Добавить новую карту")
    @ApiResponse(responseCode = "200",
            description = "Карта успешно добавлена",
            content = @Content(schema = @Schema(implementation = String.class)))
    public ResponseEntity<?> addCard(@RequestBody CardNewDTO cardNewDTO,
                                     @RequestHeader("Authorization") String token) {
        cardService.addCard(cardNewDTO, token);
        return ResponseEntity.ok("Card added successfully");
    }

    @PutMapping("/card")
    @Operation(summary = "Изменить существующую карту")
    @ApiResponse(responseCode = "200",
            description = "Адрес успешно изменен",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    @ApiResponse(responseCode = "403",
            description = "Действие запрещено",
            content = @Content(schema = @Schema(implementation = ForbiddenActionException.class)))
    public ResponseEntity<?> changeCard(@RequestBody CardDTO cardDTO,
                                        @RequestHeader("Authorization") String token) {
        cardService.changeCard(cardDTO, token);
        return ResponseEntity.ok("Card changed successfully");
    }

    @DeleteMapping("/card")
    @Operation(summary = "Удалить существующую карту")
    @ApiResponse(responseCode = "200",
            description = "Карта успешно удалена",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    @ApiResponse(responseCode = "403",
            description = "Действие запрещено",
            content = @Content(schema = @Schema(implementation = ForbiddenActionException.class)))
    public ResponseEntity<?> deleteCard(@RequestBody IntIdRequestDTO intIdRequestDTO,
                                        @RequestHeader("Authorization") String token) {
        cardService.deleteCard(intIdRequestDTO, token);
        return ResponseEntity.ok("Card deleted successfully");
    }
}
