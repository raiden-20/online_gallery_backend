package ru.vsu.cs.sheina.online_gallery_backend.controller.subscription;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.sheina.online_gallery_backend.dto.artist.ArtistShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.customer.CustomerFullDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.customer.CustomerShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.UUIDRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadActionException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentialsException;
import ru.vsu.cs.sheina.online_gallery_backend.service.PublicSubscriptionService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Подписка")
@RequestMapping("/public")
public class PublicSubscription {

    private final PublicSubscriptionService publicSubscriptionService;

    @PostMapping("/action")
    @Operation(summary = "Подписаться/отписаться в зависимости от текущего статуса")
    @ApiResponse(responseCode = "200",
            description = "Статус успешно изменен",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    @ApiResponse(responseCode = "409",
            description = "Конфликтующее действие",
            content = @Content(schema = @Schema(implementation = BadActionException.class)))
    public ResponseEntity<?> actionWithSubscription(@RequestBody UUIDRequestDTO uuidRequestDTO,
                                                    @RequestHeader("Authorization") String token) {
        publicSubscriptionService.actionWithSubscription(uuidRequestDTO, token);
        return ResponseEntity.ok("Public subscription changed successfully");
    }

    @GetMapping("/subscriptions")
    @Operation(summary = "Получить подписки")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список подписок",
            content = @Content(schema = @Schema(implementation = ArtistShortDTO.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    public ResponseEntity<?> getSubscriptions(@RequestHeader("Authorization") String token) {
        List<ArtistShortDTO> artists = publicSubscriptionService.getSubscriptions(token);
        return ResponseEntity.ok(artists);
    }

    @GetMapping("/subscribers")
    @Operation(summary = "Получить подписчиков")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список подписчиков",
            content = @Content(schema = @Schema(implementation = CustomerShortDTO.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    public ResponseEntity<?> getSubscribers(@RequestHeader("Authorization") String token) {
        List<CustomerShortDTO> customers = publicSubscriptionService.getSubscribers(token);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/search/customer/object={input}")
    @Operation(summary = "Поиск подписчиков по имени")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список подписчиков",
            content = @Content(schema = @Schema(implementation = CustomerShortDTO.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    public ResponseEntity<?> searchCustomersByPublicSubscription(@PathVariable String input,
                                                                 @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(publicSubscriptionService.searchCustomerUsers(input, token));
    }

    @GetMapping("/search/artist/object={input}")
    @Operation(summary = "Поиск подписок по имени")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список подписок",
            content = @Content(schema = @Schema(implementation = CustomerShortDTO.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    public ResponseEntity<?> searchArtistsByPublicSubscription(@PathVariable String input,
                                                               @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(publicSubscriptionService.searchArtistUsers(input, token));
    }
}
