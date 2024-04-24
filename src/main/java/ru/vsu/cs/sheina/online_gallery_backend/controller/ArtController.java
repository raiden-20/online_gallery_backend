package ru.vsu.cs.sheina.online_gallery_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.sheina.online_gallery_backend.dto.art.ArtFullDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.art.ArtistArtDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.art.CommonArtDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.art.CustomerArtDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/art")
public class ArtController {

    private final ArtService artService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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

    @GetMapping("/{artId}")
    public ResponseEntity<?> getArt(@PathVariable UUID artId,
                                    @RequestHeader("Authorization") String token) {
        ArtFullDTO artFullDTO = artService.getArt(artId);
        return ResponseEntity.ok(artFullDTO);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> changeArt(@RequestPart("artId") String artId,
                                       @RequestPart("name") String name,
                                       @RequestPart("type") String type,
                                       @RequestPart(name = "photos", value = "files") List<MultipartFile> photos,
                                       @RequestPart("deletePhotoUrls") List<String> deletePhotoUrls,
                                       @RequestPart("isPrivate") String isPrivate,
                                       @RequestPart("price") String price,
                                       @RequestPart("description") String description,
                                       @RequestPart("size") String size,
                                       @RequestPart("frame") String frame,
                                       @RequestPart("tags") List<String> tags,
                                       @RequestPart("materials") List<String> materials,
                                       @RequestHeader("Authorization") String token) {
        artService.changeArt(artId, name, type, photos, deletePhotoUrls, isPrivate, price,
                description, size, frame, tags, materials, token);
        return ResponseEntity.ok("Art created successfully");
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteArt(@RequestBody IntIdRequestDTO intIdRequestDTO,
                                    @RequestHeader("Authorization") String token) {
        artService.deleteArt(intIdRequestDTO, token);
        return ResponseEntity.ok("Art deleted successfully");
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<?> getAllArtistArts(@PathVariable UUID artistId,
                                              @RequestHeader("Authorization") String token) {
        List<ArtistArtDTO> arts = artService.getArtistArt(artistId, token);
        return ResponseEntity.ok(arts);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getAllCustomerArts(@PathVariable UUID customerId,
                                                @RequestHeader("Authorization") String token) {
        List<CustomerArtDTO> arts = artService.getCustomerArt(customerId, token);
        return ResponseEntity.ok(arts);
    }

    @GetMapping("/{type}")
    public ResponseEntity<?> getArts(@PathVariable String type,
                                     @RequestHeader("Authorization") String token) {
        List<CommonArtDTO> arts = artService.getAllArtsByType(type, token);
        return ResponseEntity.ok(arts);
    }
}
