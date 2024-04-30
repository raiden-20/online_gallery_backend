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
import ru.vsu.cs.sheina.online_gallery_backend.dto.art.ArtShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.order.PurchaseDTO;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadActionException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentialsException;
import ru.vsu.cs.sheina.online_gallery_backend.service.CartService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Корзина")
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @PostMapping()
    @Operation(summary = "Добавить товар в корзину")
    @ApiResponse(responseCode = "200",
            description = "Товар добавлен в корзину",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    @ApiResponse(responseCode = "409",
            description = "Товар уже находится в корзине",
            content = @Content(schema = @Schema(implementation = BadActionException.class)))
    public ResponseEntity<?> addArtInCart(@RequestBody IntIdRequestDTO intIdRequestDTO,
                                          @RequestHeader("Authorization") String token) {
        cartService.addArt(intIdRequestDTO, token);
        return ResponseEntity.ok("Art added to cart");
    }

    @DeleteMapping()
    @Operation(summary = "Удалить товар из корзины")
    @ApiResponse(responseCode = "200",
            description = "Товар удален из корзины",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "409",
            description = "Данного товара нет в корзине",
            content = @Content(schema = @Schema(implementation = BadActionException.class)))
    public ResponseEntity<?> deleteArtFromCart(@RequestBody IntIdRequestDTO intIdRequestDTO,
                                               @RequestHeader("Authorization") String token) {
        cartService.deleteArtFromCart(intIdRequestDTO, token);
        return ResponseEntity.ok("Art deleted from cart");
    }

    @GetMapping()
    @Operation(summary = "Получить все товары в корзине")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список товаров",
            content = @Content(schema = @Schema(implementation = ArtShortDTO.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    public ResponseEntity<?> getCartData(@RequestHeader("Authorization") String token) {
        List<ArtShortDTO> arts = cartService.getCartData(token);
        return ResponseEntity.ok(arts);
    }

    @PostMapping("/buy")
    @Operation(summary = "Оплатить товары из корзины")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список id созданных заказов",
            content = @Content(schema = @Schema(implementation = Integer.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    public ResponseEntity<?> buyArtsFromCart(@RequestBody PurchaseDTO purchaseDTO,
                                             @RequestHeader("Authorization") String token) {
        List<Integer> orderIds = cartService.buy(purchaseDTO, token);
        return ResponseEntity.ok(orderIds);
    }
}
