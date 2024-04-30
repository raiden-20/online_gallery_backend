package ru.vsu.cs.sheina.online_gallery_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.sheina.online_gallery_backend.dto.artist.ArtistFullDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.artist.ArtistRegistrationDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.artist.ArtistShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.customer.CustomerFullDTO;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadActionException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentialsException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.ForbiddenActionException;
import ru.vsu.cs.sheina.online_gallery_backend.service.ArtistService;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Художник")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    @GetMapping("/artist/artistId={artistId}&currentId={currentId}")
    @Operation(summary = "Получить данные художника")
    @ApiResponse(responseCode = "200",
            description = "Отправлены данные художника",
            content = @Content(schema = @Schema(implementation = ArtistFullDTO.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    public ResponseEntity<?> getArtistData(@PathVariable UUID artistId,
                                           @PathVariable String currentId) {
        ArtistFullDTO artistFullDTO = artistService.getArtistData(artistId, currentId);
        return ResponseEntity.ok(artistFullDTO);
    }

    @PutMapping(value = "/artist/data", consumes = "multipart/form-data")
    @Operation(summary = "Изменить данные художника")
    @ApiResponse(responseCode = "200",
            description = "Данные успешно изменены",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    public ResponseEntity<?> setArtistData(@RequestPart("artistName") String artistName,
                                           @RequestPart("avatarUrl") String avatarUrl,
                                           @RequestPart("coverUrl") String coverUrl,
                                           @RequestPart("description") String description,
                                           @RequestPart("avatar") MultipartFile avatar,
                                           @RequestPart("cover") MultipartFile cover,
                                           @RequestHeader("Authorization") String token) {
        artistService.setArtistData(token, artistName, avatarUrl, coverUrl, description, avatar, cover);
        return ResponseEntity.ok("Data updated successfully");
    }

    @PostMapping("/artist/create")
    @Operation(summary = "Создать аккаунт художника")
    @ApiResponse(responseCode = "200",
            description = "Аккаунт успешно создан",
            content = @Content(schema = @Schema(implementation = UUID.class)))
    @ApiResponse(responseCode = "409",
            description = "Статус заказа не соответствует действию",
            content = @Content(schema = @Schema(implementation = BadActionException.class)))
    public ResponseEntity<?> createArtist(@RequestBody ArtistRegistrationDTO artistRegistrationDTO,
                                          @RequestHeader("Authorization") String token){
        UUID id = artistService.createArtist(artistRegistrationDTO, token);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/artists")
    @Operation(summary = "Получить всех художников")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список художников",
            content = @Content(schema = @Schema(implementation = ArtistShortDTO.class)))
    public ResponseEntity<?> getArtists() {
        List<ArtistShortDTO> artistList = artistService.getArtists();
        return ResponseEntity.ok(artistList);
    }

    @GetMapping("/search/artist/object={input}")
    @Operation(summary = "Поиск художников по имени")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список художников",
            content = @Content(schema = @Schema(implementation = ArtistShortDTO.class)))
    public ResponseEntity<?> searchArtist(@PathVariable String input) {
        List<ArtistShortDTO> artists = artistService.searchArtist(input);
        return ResponseEntity.ok(artists);
    }
}
