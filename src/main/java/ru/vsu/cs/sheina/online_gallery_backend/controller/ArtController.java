package ru.vsu.cs.sheina.online_gallery_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.sheina.online_gallery_backend.dto.art.*;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.service.ArtService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ArtController {

    private final ArtService artService;

    @PostMapping(value = "/art", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE,
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> createArt(@RequestPart("ArtCreateDTO") ArtCreateDTO artCreateDTO,
                                       @RequestPart(value = "photos") List<MultipartFile> photos,
                                       @RequestHeader("Authorization") String token) {
        artService.createArt(artCreateDTO, photos, token);
        return ResponseEntity.ok("Art created successfully");
    }

    @GetMapping("/art/artId={artId}&currentId={currentId}")
    public ResponseEntity<?> getArt(@PathVariable Integer artId,
                                    @PathVariable String currentId) {
        ArtFullDTO artFullDTO = artService.getArt(artId, currentId);
        return ResponseEntity.ok(artFullDTO);
    }

    @PutMapping(value = "/art", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE,
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> changeArt(@RequestPart("ArtChangeDTO") ArtChangeDTO artChangeDTO,
                                       @RequestPart(value = "newPhotos") List<MultipartFile> newPhotos,
                                       @RequestHeader("Authorization") String token) {
        artService.changeArt(artChangeDTO, newPhotos, token);
        return ResponseEntity.ok("Art changed successfully");
    }

    @DeleteMapping("/art")
    public ResponseEntity<?> deleteArt(@RequestBody IntIdRequestDTO intIdRequestDTO,
                                       @RequestHeader("Authorization") String token) {
        artService.deleteArt(intIdRequestDTO, token);
        return ResponseEntity.ok("Art deleted successfully");
    }

    @GetMapping("/art/artist/artistId={artistId}&currentId={currentId}")
    public ResponseEntity<?> getAllArtistArts(@PathVariable UUID artistId,
                                              @PathVariable String currentId) {
        List<ArtistArtDTO> arts = artService.getArtistArt(artistId, currentId);
        return ResponseEntity.ok(arts);
    }

    @GetMapping("/art/customer/{customerId}")
    public ResponseEntity<?> getAllCustomerArts(@PathVariable UUID customerId) {
        List<CustomerArtDTO> arts = artService.getCustomerArt(customerId);
        return ResponseEntity.ok(arts);
    }

    @GetMapping("/{type}")
    public ResponseEntity<?> getArts(@PathVariable String type) {
        List<CommonArtDTO> arts = artService.getAllArtsByType(type);
        return ResponseEntity.ok(arts);
    }

    @GetMapping("/search/paintings/object={input}")
    public ResponseEntity<?> searchPaintings(@PathVariable String input) {
        List<CommonArtDTO> arts = artService.searchPaintings(input);
        return ResponseEntity.ok(arts);
    }

    @GetMapping("/search/photos/object={input}")
    public ResponseEntity<?> searchPhotos(@PathVariable String input) {
        List<CommonArtDTO> arts = artService.searchPhotos(input);
        return ResponseEntity.ok(arts);
    }

    @GetMapping("/search/sculptures/object={input}")
    public ResponseEntity<?> searchSculptures(@PathVariable String input) {
        List<CommonArtDTO> arts = artService.searchSculptures(input);
        return ResponseEntity.ok(arts);
    }
}
