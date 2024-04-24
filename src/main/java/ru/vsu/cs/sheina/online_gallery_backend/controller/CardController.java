package ru.vsu.cs.sheina.online_gallery_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.sheina.online_gallery_backend.dto.card.CardDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.card.CardNewDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;

@RestController
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping("/cards")
    public ResponseEntity<?> getCards(@RequestHeader("Authorization") String token) {
        List<CardDTO> cards = cardService.getCards(token);
        return ResponseEntity.ok(cards);
    }

    @PostMapping("/card")
    public ResponseEntity<?> addCard(@RequestBody CardNewDTO cardNewDTO,
                                     @RequestHeader("Authorization") String token) {
        cardService.addCard(cardNewDTO, token);
        return ResponseEntity.ok("Card added successfully");
    }

    @PutMapping("/card")
    public ResponseEntity<?> changeCard(@RequestBody CardDTO cardDTO,
                                        @RequestHeader("Authorization") String token) {
        cardService.changeCard(cardDTO, token);
        return ResponseEntity.ok("Card changed successfully");
    }

    @DeleteMapping("/card")
    public ResponseEntity<?> deleteCard(@RequestBody IntIdRequestDTO intIdRequestDTO,
                                        @RequestHeader("Authorization") String token) {
        cardService.deleteCard(intIdRequestDTO, token);
        return ResponseEntity.ok("Card deleted successfully");
    }
}
