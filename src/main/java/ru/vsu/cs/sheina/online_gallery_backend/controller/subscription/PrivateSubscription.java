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
import ru.vsu.cs.sheina.online_gallery_backend.dto.customer.CustomerShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.UUIDRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.subscription.PriceDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.subscription.SubscribeDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.subscription.PrivateSubscriptionDTO;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadActionException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentialsException;
import ru.vsu.cs.sheina.online_gallery_backend.service.PrivateSubscriptionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Платная подписка")
@RequestMapping("/api/private")
public class PrivateSubscription {

    private final PrivateSubscriptionService privateSubscriptionService;

    @PostMapping("/subscribe")
    @Operation(summary = "Платно подписаться на художника")
    @ApiResponse(responseCode = "200",
            description = "Подписка успешно оформлена",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    @ApiResponse(responseCode = "409",
            description = "Конфликтующее действие",
            content = @Content(schema = @Schema(implementation = BadActionException.class)))
    public ResponseEntity<?> subscribe(@RequestBody SubscribeDTO subscribeDTO,
                                       @RequestHeader("Authorization") String token) {
        privateSubscriptionService.subscribe(subscribeDTO, token);
        return ResponseEntity.ok("You subscribed successfully");
    }

    @DeleteMapping("/unsubscribe")
    @Operation(summary = "Удалить платную подписку на художника")
    @ApiResponse(responseCode = "200",
            description = "Подписка успешно удалена",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    @ApiResponse(responseCode = "409",
            description = "Конфликтующее действие",
            content = @Content(schema = @Schema(implementation = BadActionException.class)))
    public ResponseEntity<?> unsubscribe(@RequestBody UUIDRequestDTO uuidRequestDTO,
                                         @RequestHeader("Authorization") String token) {
        privateSubscriptionService.unsubscribe(uuidRequestDTO, token);
        return ResponseEntity.ok("You unsubscribed successfully");
    }

    @GetMapping("/subscriptions")
    @Operation(summary = "Получить платные подписки")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список платных подписок",
            content = @Content(schema = @Schema(implementation = PrivateSubscriptionDTO.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    public ResponseEntity<?> getSubscriptions(@RequestHeader("Authorization") String token) {
        List<PrivateSubscriptionDTO> artists = privateSubscriptionService.getSubscriptions(token);
        return ResponseEntity.ok(artists);
    }

    @PostMapping("/create")
    @Operation(summary = "Создать платную подписку")
    @ApiResponse(responseCode = "200",
            description = "Платная подписка успешно создана",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    @ApiResponse(responseCode = "409",
            description = "Конфликтующее действие",
            content = @Content(schema = @Schema(implementation = BadActionException.class)))
    public ResponseEntity<?> createSubscription(@RequestBody PriceDTO priceDTO) {
        privateSubscriptionService.createSubscription(priceDTO);
        return ResponseEntity.ok("Private subscription created successfully");
    }

    @GetMapping("/{artistId}")
    @Operation(summary = "Получить данные подписки")
    @ApiResponse(responseCode = "200",
            description = "Отправлены данные о платной подписке",
            content = @Content(schema = @Schema(implementation = PriceDTO.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    public ResponseEntity<?> getSubscriptionData(@PathVariable UUID artistId) {
        PriceDTO priceDTO = privateSubscriptionService.getSubscriptionData(artistId);
        return ResponseEntity.ok(priceDTO);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Удалить платную подписку")
    @ApiResponse(responseCode = "200",
            description = "Платная подписка успешно удалена",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    public ResponseEntity<?> deleteSubscription(@RequestHeader("Authorization") String token) {
        privateSubscriptionService.deleteSubscription(token);
        return ResponseEntity.ok("Private subscription deleted successfully");
    }

    @GetMapping("/subscribers")
    @Operation(summary = "Получить список подписчиков")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список подписчиков",
            content = @Content(schema = @Schema(implementation = CustomerShortDTO.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    public ResponseEntity<?> getSubscribers(@RequestHeader("Authorization") String token) {
        List<CustomerShortDTO> customers = privateSubscriptionService.getSubscribers(token);
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
        return ResponseEntity.ok(privateSubscriptionService.searchCustomerUsers(input, token));
    }

    @GetMapping("/search/artist/object={input}")
    @Operation(summary = "Поиск подписок по имени")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список подписок",
            content = @Content(schema = @Schema(implementation = PrivateSubscriptionDTO.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    public ResponseEntity<?> searchArtistsByPublicSubscription(@PathVariable String input,
                                                        @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(privateSubscriptionService.searchArtistUsers(input, token));
    }
}
