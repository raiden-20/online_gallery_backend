package ru.vsu.cs.sheina.online_gallery_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.order.OrderDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.order.OrderShortDTO;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/orders/{userId}")
    public ResponseEntity<?> getAllOrders(@PathVariable UUID userId,
                                          @RequestHeader("Authorization") String token) {
        List<OrderDTO> orders = orderService.getOrders(userId, token);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getAllOrders(@PathVariable Integer orderId,
                                          @RequestHeader("Authorization") String token) {
        OrderDTO order = orderService.getOrder(orderId, token);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/order/receive")
    public ResponseEntity<?> receive(@RequestBody IntIdRequestDTO intIdDTO,
                                     @RequestHeader("Authorization") String token) {
        orderService.receive(intIdDTO, token);
        return ResponseEntity.ok("Product received");
    }

    @PostMapping("/order/send")
    public ResponseEntity<?> send(@RequestBody OrderShortDTO orderShortDTO,
                                  @RequestHeader("Authorization") String token) {
        orderService.send(orderShortDTO, token);
        return ResponseEntity.ok("Product sent");
    }

    @PutMapping("/order/edit")
    public ResponseEntity<?> edit(@RequestBody OrderShortDTO orderShortDTO,
                                  @RequestHeader("Authorization") String token) {
        orderService.edit(orderShortDTO, token);
        return ResponseEntity.ok("Order changed");
    }

}
