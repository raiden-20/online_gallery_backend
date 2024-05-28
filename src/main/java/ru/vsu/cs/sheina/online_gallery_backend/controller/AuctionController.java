package ru.vsu.cs.sheina.online_gallery_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import ru.vsu.cs.sheina.online_gallery_backend.dto.auction.*;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.service.AuctionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuctionController {

    private final AuctionService auctionService;

    @PostMapping(value = "/auction", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE,
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> createAuction(@RequestPart("AuctionCreateDTO") AuctionCreateDTO auctionCreateDTO,
                                           @RequestPart(value = "photos") List<MultipartFile> photos,
                                           @RequestHeader("Authorization") String token) {
        Integer auctionId = auctionService.createAuction(auctionCreateDTO, photos, token);
        return ResponseEntity.ok(auctionId);
    }

    @PutMapping(value = "/auction", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE,
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> changeAuction(@RequestPart("AuctionChangeDTO") AuctionChangeDTO auctionChangeDTO,
                                           @RequestPart(value = "newPhotos") List<MultipartFile> newPhotos,
                                           @RequestHeader("Authorization") String token) {
        auctionService.changeAuction(auctionChangeDTO, newPhotos, token);
        return ResponseEntity.ok("Auction changed successfully");
    }

    @DeleteMapping("/auction")
    public ResponseEntity<?> deleteAuction(@RequestBody IntIdRequestDTO intIdRequestDTO,
                                           @RequestHeader("Authorization") String token) {
        auctionService.deleteAuction(intIdRequestDTO, token);
        return ResponseEntity.ok("Auction deleted successfully");
    }

    @GetMapping("/auction/auctionId={auctionId}&currentId={currentId}")
    public ResponseEntity<?> getAuction(@PathVariable Integer auctionId,
                                        @PathVariable String currentId) {
        AuctionFullDTO auctionFullDTO = auctionService.getAuction(auctionId, currentId);
        return ResponseEntity.ok(auctionFullDTO);
    }

    @GetMapping("/auction/artist/{artistId}")
    public ResponseEntity<?> getAllArtistAuction(@PathVariable UUID artistId){
        List<AuctionShortDTO> auctions = auctionService.getArtistAuctions(artistId);
        return ResponseEntity.ok(auctions);
    }

    @GetMapping("/auctions")
    public ResponseEntity<?> getAllAuctions(){
        List<AuctionShortDTO> auctions = auctionService.getAllAuctions();
        return ResponseEntity.ok(auctions);
    }

    @PostMapping("/auction/maxrate")
    public ResponseEntity<?> createMaxRate(@RequestBody MaxRateCreateDTO maxRateCreateDTO,
                                           @RequestHeader("Authorization") String token) {
        auctionService.createMaxRate(maxRateCreateDTO, token);
        return ResponseEntity.ok("Max rate created successfully");
    }

    @PostMapping("/auction/rate")
    public ResponseEntity<?> createRate(@RequestBody RateCreateDTO rateCreateDTO,
                                        @RequestHeader("Authorization") String token) {
        auctionService.createRate(rateCreateDTO, token);
        return ResponseEntity.ok("Rate created successfully");
    }

    @GetMapping(value = "/auction/rates/userId={userId}&auctionId={auctionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent> openSseStream(@PathVariable UUID userId, @PathVariable Integer auctionId) {

        return Flux.create(fluxSink -> {
            fluxSink.onCancel(
                    () -> {
                        auctionService.deleteUserFromSubscriptions(userId, auctionId);
                    }
            );
            auctionService.addUserToSubscriptions(userId, auctionId, fluxSink);
        });
    }

    @GetMapping("/search/auction/object={input}")
    public ResponseEntity<?> searchAuctions(@PathVariable String input){
        List<AuctionShortDTO> auctions = auctionService.searchAuctions(input);
        return ResponseEntity.ok(auctions);
    }
}
