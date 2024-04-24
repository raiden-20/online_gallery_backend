package ru.vsu.cs.sheina.online_gallery_backend.controller.subscription;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.sheina.online_gallery_backend.dto.ArtistShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.CustomerShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.UUIDRequestDTO;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public")
public class PublicSubscription {

    private final PublicSubscriptionService publicSubscriptionService;

    @PostMapping("/action")
    public ResponseEntity<?> actionWithSubscription(@RequestBody UUIDRequestDTO uuidRequestDTO,
                                                @RequestHeader("Authorization") String token) {
        publicSubscriptionService.actionWithSubscription(uuidRequestDTO, token);
        return ResponseEntity.ok("Public subscription changed successfully");
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<?> getSubscriptions(@RequestHeader("Authorization") String token) {
        List<ArtistShortDTO> artists = publicSubscriptionService.getSubscriptions(token);
        return ResponseEntity.ok(artists);
    }

    @GetMapping("/subscribers")
    public ResponseEntity<?> getSubscribers(@RequestHeader("Authorization") String token) {
        List<CustomerShortDTO> customers = publicSubscriptionService.getSubscribers(token);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/search/{role}/object={input}")
    public ResponseEntity<?> searchByPublicSubscription(@PathVariable String role,
                                                        @PathVariable String input,
                                                        @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(publicSubscriptionService.getUsers(role, input, token));
    }
}
