package ru.vsu.cs.sheina.online_gallery_backend.controller.subscription;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.sheina.online_gallery_backend.dto.CustomerShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.subscription.PriceDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.subscription.SubscribeDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.subscription.PrivateSubscriptionDTO;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/private")
public class PrivateSubscription {

    private final PrivateSubscriptionService privateSubscriptionService;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody SubscribeDTO subscribeDTO,
                                             @RequestHeader("Authorization") String token) {
        privateSubscriptionService.subscribe(subscribeDTO, token);
        return ResponseEntity.ok("You subscribed successfully");
    }

    @DeleteMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestBody IntIdRequestDTO intIdRequestDTO,
                                             @RequestHeader("Authorization") String token) {
        privateSubscriptionService.unsubscribe(intIdRequestDTO, token);
        return ResponseEntity.ok("You unsubscribed successfully");
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<?> getSubscriptions(@RequestHeader("Authorization") String token) {
        List<PrivateSubscriptionDTO> artists = privateSubscriptionService.getSubscriptions(token);
        return ResponseEntity.ok(artists);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createSubscription(@RequestBody PriceDTO priceDTO,
                                                @RequestHeader("Authorization") String token) {
        privateSubscriptionService.createSubscription(priceDTO, token);
        return ResponseEntity.ok("Private subscription created successfully");
    }

    @GetMapping("/{artistId}")
    public ResponseEntity<?> getSubscriptionData(@PathVariable UUID artistId) {
        PriceDTO priceDTO = privateSubscriptionService.getSubscriptionData(artistId);
        return ResponseEntity.ok(priceDTO);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteSubscription(@RequestHeader("Authorization") String token) {
        privateSubscriptionService.deleteSubscription(token);
        return ResponseEntity.ok("Private subscription deleted successfully");
    }

    @GetMapping("/subscribers")
    public ResponseEntity<?> getSubscribers(@RequestHeader("Authorization") String token) {
        List<CustomerShortDTO> customers = privateSubscriptionService.getSubscribers(token);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/search/{role}/object={input}")
    public ResponseEntity<?> searchByPublicSubscription(@PathVariable String role,
                                                        @PathVariable String input,
                                                        @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(privateSubscriptionService.getUsers(role, input, token));
    }
}
