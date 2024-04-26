package ru.vsu.cs.sheina.online_gallery_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.sheina.online_gallery_backend.dto.ArtistShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.art.*;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.service.ArtService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ArtController {

    private final ArtService artService;

    @PostMapping(value = "/art", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createArt(@RequestPart("name") String name,
                                       @RequestPart("type") String type,
                                       @RequestPart(name = "photos", value = "files") List<MultipartFile> photos,
                                       @RequestPart("isPrivate") String isPrivate,
                                       @RequestPart("price") String price,
                                       @RequestPart("description") String description,
                                       @RequestPart("size") String size,
                                       @RequestPart("frame") String frame,
                                       @RequestPart("tags") List<String> tags,
                                       @RequestPart("materials") List<String> materials,
                                       @RequestHeader("Authorization") String token
                                       ) {
        artService.createArt(name, type, photos, isPrivate, price,
                                description, size, frame, tags, materials, token);
        return ResponseEntity.ok("Art created successfully");
    }

    @GetMapping("/art/{artId}")
    public ResponseEntity<?> getArt(@PathVariable Integer artId,
                                    @RequestHeader("Authorization") String token) {
        ArtFullDTO artFullDTO = artService.getArt(artId, token);
        return ResponseEntity.ok(artFullDTO);
    }

    @PutMapping(value = "/art", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> changeArt(@RequestPart("artId") String artId,
                                       @RequestPart("name") String name,
                                       @RequestPart("type") String type,
                                       @RequestPart(name = "newPhotos", value = "files") List<MultipartFile> newPhotos,
                                       @RequestPart("deletePhotoUrls") List<String> deletePhotoUrls,
                                       @RequestPart("changeMainPhoto") String changeMainPhoto,
                                       @RequestPart("isPrivate") String isPrivate,
                                       @RequestPart("price") String price,
                                       @RequestPart("description") String description,
                                       @RequestPart("size") String size,
                                       @RequestPart("frame") String frame,
                                       @RequestPart("tags") List<String> tags,
                                       @RequestPart("materials") List<String> materials,
                                       @RequestHeader("Authorization") String token) {
        artService.changeArt(artId, name, type, newPhotos, deletePhotoUrls, changeMainPhoto, isPrivate, price,
                description, size, frame, tags, materials, token);
        return ResponseEntity.ok("Art created successfully");
    }

    @DeleteMapping("/art")
    public ResponseEntity<?> deleteArt(@RequestBody IntIdRequestDTO intIdRequestDTO,
                                       @RequestHeader("Authorization") String token) {
        artService.deleteArt(intIdRequestDTO, token);
        return ResponseEntity.ok("Art deleted successfully");
    }

    @GetMapping("/art/artist/{artistId}")
    public ResponseEntity<?> getAllArtistArts(@PathVariable UUID artistId,
                                              @RequestHeader("Authorization") String token) {
        List<ArtistArtDTO> arts = artService.getArtistArt(artistId, token);
        return ResponseEntity.ok(arts);
    }

    @GetMapping("/art/customer/{customerId}")
    public ResponseEntity<?> getAllCustomerArts(@PathVariable UUID customerId) {
        List<CustomerArtDTO> arts = artService.getCustomerArt(customerId);
        return ResponseEntity.ok(arts);
    }

    @GetMapping("/art/{type}")
    public ResponseEntity<?> getArts(@PathVariable String type) {
        List<CommonArtDTO> arts = artService.getAllArtsByType(type);
        return ResponseEntity.ok(arts);
    }

    @GetMapping("/search/art/object={input}")
    public ResponseEntity<?> searchArtist(@PathVariable String input) {
        List<ArtShortDTO> arts = artService.searchArt(input);
        return ResponseEntity.ok(arts);
    }
}
