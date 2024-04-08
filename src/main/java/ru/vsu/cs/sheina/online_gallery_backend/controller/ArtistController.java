package ru.vsu.cs.sheina.online_gallery_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.sheina.online_gallery_backend.dto.ArtistFullDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.ArtistRegistrationDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.ArtistShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.service.ArtistService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    @GetMapping("/artist/{id}")
    public ResponseEntity<?> getArtistData(@PathVariable UUID id) {
        ArtistFullDTO artistFullDTO = artistService.getArtistData(id);
        return ResponseEntity.ok(artistFullDTO);
    }

    @PutMapping(value = "/artist/data", consumes = "multipart/form-data")
    public ResponseEntity<?> setArtistData(@RequestPart("artistId") String artistId,
                                           @RequestPart("artistName") String artistName,
                                           @RequestPart("avatarUrl") String avatarUrl,
                                           @RequestPart("coverUrl") String coverUrl,
                                           @RequestPart("description") String description,
                                           @RequestPart("avatar") MultipartFile avatar,
                                           @RequestPart("cover") MultipartFile cover) {
        artistService.setArtistData(artistId, artistName, avatarUrl, coverUrl, description, avatar, cover);
        return ResponseEntity.ok("Data updated successfully");
    }

    @PostMapping("/artist/create")
    public ResponseEntity<?> createArtist(@RequestBody ArtistRegistrationDTO artistRegistrationDTO){
        UUID id = artistService.createArtist(artistRegistrationDTO);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/artists")
    public ResponseEntity<?> getArtists() {
        List<ArtistShortDTO> artistList = artistService.getArtists();
        return ResponseEntity.ok(artistList);
    }

    @GetMapping("/search/artist/object={input}")
    public ResponseEntity<?> searchArtist(@PathVariable String input) {
        List<ArtistShortDTO> artists = artistService.searchArtist(input);
        return ResponseEntity.ok(artists);
    }
}
