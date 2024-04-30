package ru.vsu.cs.sheina.online_gallery_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.order.OrderDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.order.OrderShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadActionException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentialsException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.ForbiddenActionException;
import ru.vsu.cs.sheina.online_gallery_backend.service.OrderService;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Заказы")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/orders/{userId}")
    @Operation(summary = "Получить все заказы пользователя")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список заказов",
            content = @Content(schema = @Schema(implementation = OrderDTO.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    @ApiResponse(responseCode = "403",
            description = "Действие запрещено",
            content = @Content(schema = @Schema(implementation = ForbiddenActionException.class)))
    public ResponseEntity<?> getAllOrders(@PathVariable UUID userId,
                                          @RequestHeader("Authorization") String token) {
        List<OrderDTO> orders = orderService.getOrders(userId, token);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Получить данные одного заказа")
    @ApiResponse(responseCode = "200",
            description = "Отправлены данные заказа",
            content = @Content(schema = @Schema(implementation = OrderDTO.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    @ApiResponse(responseCode = "403",
            description = "Действие запрещено",
            content = @Content(schema = @Schema(implementation = ForbiddenActionException.class)))
    public ResponseEntity<?> getOrder(@PathVariable Integer orderId,
                                          @RequestHeader("Authorization") String token) {
        OrderDTO order = orderService.getOrder(orderId, token);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/order/receive")
    @Operation(summary = "Сменить статус заказа на \"Получено\"")
    @ApiResponse(responseCode = "200",
            description = "Статус изменен",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    @ApiResponse(responseCode = "403",
            description = "Действие запрещено",
            content = @Content(schema = @Schema(implementation = ForbiddenActionException.class)))
    @ApiResponse(responseCode = "409",
            description = "Статус заказа не соответствует действию",
            content = @Content(schema = @Schema(implementation = BadActionException.class)))
    public ResponseEntity<?> receive(@RequestBody IntIdRequestDTO intIdDTO,
                                     @RequestHeader("Authorization") String token) {
        orderService.receive(intIdDTO, token);
        return ResponseEntity.ok("Product received");
    }

    @PostMapping("/order/send")
    @Operation(summary = "Сменить статус заказа на \"Отправлено\"")
    @ApiResponse(responseCode = "200",
            description = "Статус изменен",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    @ApiResponse(responseCode = "403",
            description = "Действие запрещено",
            content = @Content(schema = @Schema(implementation = ForbiddenActionException.class)))
    public ResponseEntity<?> send(@RequestBody OrderShortDTO orderShortDTO,
                                  @RequestHeader("Authorization") String token) {
        orderService.send(orderShortDTO, token);
        return ResponseEntity.ok("Product sent");
    }

    @PutMapping("/order/edit")
    @Operation(summary = "Изменить комментарий у отправленного заказа")
    @ApiResponse(responseCode = "200",
            description = "Комментарий изменен",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    @ApiResponse(responseCode = "403",
            description = "Действие запрещено",
            content = @Content(schema = @Schema(implementation = ForbiddenActionException.class)))
    @ApiResponse(responseCode = "409",
            description = "Статус заказа не соответствует действию",
            content = @Content(schema = @Schema(implementation = ForbiddenActionException.class)))
    public ResponseEntity<?> edit(@RequestBody OrderShortDTO orderShortDTO,
                                  @RequestHeader("Authorization") String token) {
        orderService.edit(orderShortDTO, token);
        return ResponseEntity.ok("Order changed");
    }

}
