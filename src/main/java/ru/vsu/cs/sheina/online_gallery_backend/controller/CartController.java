package ru.vsu.cs.sheina.online_gallery_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.sheina.online_gallery_backend.dto.art.ArtShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.order.PurchaseDTO;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @PostMapping()
    public ResponseEntity<?> addArtInCart(@RequestBody IntIdRequestDTO intIdRequestDTO,
                                          @RequestHeader("Authorization") String token) {
        cartService.addArt(intIdRequestDTO, token);
        return ResponseEntity.ok("Art added to cart");
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteArtFromCart(@RequestBody IntIdRequestDTO intIdRequestDTO,
                                               @RequestHeader("Authorization") String token) {
        cartService.deleteArt(intIdRequestDTO, token);
        return ResponseEntity.ok("Art deleted from cart");
    }

    @GetMapping()
    public ResponseEntity<?> getCartData(@RequestHeader("Authorization") String token) {
        List<ArtShortDTO> arts = cartService.getCartData(token);
        return ResponseEntity.ok(arts);
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buyArtsFromCart(@RequestBody PurchaseDTO purchaseDTO,
                                             @RequestHeader("Authorization") String token) {
        cartService.buy(purchaseDTO, token);
        return ResponseEntity.ok("Purchase completed successfully");
    }
}
