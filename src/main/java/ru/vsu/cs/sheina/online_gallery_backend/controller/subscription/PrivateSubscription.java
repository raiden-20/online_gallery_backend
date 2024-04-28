package ru.vsu.cs.sheina.online_gallery_backend.controller.subscription;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.sheina.online_gallery_backend.dto.customer.CustomerShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.UUIDRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.subscription.PriceDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.subscription.SubscribeDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.subscription.PrivateSubscriptionDTO;
import ru.vsu.cs.sheina.online_gallery_backend.service.PrivateSubscriptionService;

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
    public ResponseEntity<?> unsubscribe(@RequestBody UUIDRequestDTO uuidRequestDTO,
                                         @RequestHeader("Authorization") String token) {
        privateSubscriptionService.unsubscribe(uuidRequestDTO, token);
        return ResponseEntity.ok("You unsubscribed successfully");
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<?> getSubscriptions(@RequestHeader("Authorization") String token) {
        List<PrivateSubscriptionDTO> artists = privateSubscriptionService.getSubscriptions(token);
        return ResponseEntity.ok(artists);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createSubscription(@RequestBody PriceDTO priceDTO) {
        privateSubscriptionService.createSubscription(priceDTO);
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

    @GetMapping("/search/customer/object={input}")
    public ResponseEntity<?> searchCustomersByPublicSubscription(@PathVariable String input,
                                                        @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(privateSubscriptionService.searchCustomerUsers(input, token));
    }

    @GetMapping("/search/artist/object={input}")
    public ResponseEntity<?> searchArtistsByPublicSubscription(@PathVariable String input,
                                                        @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(privateSubscriptionService.searchArtistUsers(input, token));
    }
}
