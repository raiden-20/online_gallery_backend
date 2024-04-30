package ru.vsu.cs.sheina.online_gallery_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.sheina.online_gallery_backend.dto.art.*;
import ru.vsu.cs.sheina.online_gallery_backend.dto.artist.ArtistFullDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadActionException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentialsException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.ForbiddenActionException;
import ru.vsu.cs.sheina.online_gallery_backend.service.ArtService;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Произведение искусства")
@RequiredArgsConstructor
public class ArtController {

    private final ArtService artService;

    @PostMapping(value = "/art", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE,
            MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Добавить товар")
    @ApiResponse(responseCode = "200",
            description = "Товар успешно добавлен",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    @ApiResponse(responseCode = "409",
            description = "Конфликтующее действие",
            content = @Content(schema = @Schema(implementation = BadActionException.class)))
    public ResponseEntity<?> createArt(@RequestPart("ArtCreateDTO") ArtCreateDTO artCreateDTO,
                                       @RequestPart(value = "photos") List<MultipartFile> photos,
                                       @RequestHeader("Authorization") String token) {
        artService.createArt(artCreateDTO, photos, token);
        return ResponseEntity.ok("Art created successfully");
    }

    @GetMapping("/art/artId={artId}&currentId={currentId}")
    @Operation(summary = "Получить товар")
    @ApiResponse(responseCode = "200",
            description = "Отправлены данные товара",
            content = @Content(schema = @Schema(implementation = ArtFullDTO.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    @ApiResponse(responseCode = "403",
            description = "Действие запрещено",
            content = @Content(schema = @Schema(implementation = ForbiddenActionException.class)))
    public ResponseEntity<?> getArt(@PathVariable Integer artId,
                                    @PathVariable String currentId) {
        ArtFullDTO artFullDTO = artService.getArt(artId, currentId);
        return ResponseEntity.ok(artFullDTO);
    }

    @PutMapping(value = "/art", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE,
            MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Изменить данные товара")
    @ApiResponse(responseCode = "200",
            description = "Данные успешно изменены",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    @ApiResponse(responseCode = "403",
            description = "Действие запрещено",
            content = @Content(schema = @Schema(implementation = ForbiddenActionException.class)))
    @ApiResponse(responseCode = "409",
            description = "Конфликтующее действие",
            content = @Content(schema = @Schema(implementation = BadActionException.class)))
    public ResponseEntity<?> changeArt(@RequestPart("ArtChangeDTO") ArtChangeDTO artChangeDTO,
                                       @RequestPart(value = "newPhotos") List<MultipartFile> newPhotos,
                                       @RequestHeader("Authorization") String token) {
        artService.changeArt(artChangeDTO, newPhotos, token);
        return ResponseEntity.ok("Art changed successfully");
    }

    @DeleteMapping("/art")
    @Operation(summary = "Удалить товар")
    @ApiResponse(responseCode = "200",
            description = "Товар успешно удален",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    @ApiResponse(responseCode = "403",
            description = "Действие запрещено",
            content = @Content(schema = @Schema(implementation = ForbiddenActionException.class)))
    public ResponseEntity<?> deleteArt(@RequestBody IntIdRequestDTO intIdRequestDTO,
                                       @RequestHeader("Authorization") String token) {
        artService.deleteArt(intIdRequestDTO, token);
        return ResponseEntity.ok("Art deleted successfully");
    }

    @GetMapping("/art/artist/artistId={artistId}&currentId={currentId}")
    @Operation(summary = "Получить все товары художника")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список товаров художника",
            content = @Content(schema = @Schema(implementation = ArtistArtDTO.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    public ResponseEntity<?> getAllArtistArts(@PathVariable UUID artistId,
                                              @PathVariable String currentId) {
        List<ArtistArtDTO> arts = artService.getArtistArt(artistId, currentId);
        return ResponseEntity.ok(arts);
    }

    @GetMapping("/art/customer/{customerId}")
    @Operation(summary = "Получить все товары покупателя")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список товаров покупателя",
            content = @Content(schema = @Schema(implementation = CustomerArtDTO.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    public ResponseEntity<?> getAllCustomerArts(@PathVariable UUID customerId) {
        List<CustomerArtDTO> arts = artService.getCustomerArt(customerId);
        return ResponseEntity.ok(arts);
    }

    @GetMapping("/{type}")
    @Operation(summary = "Получить все товары в зависимости от типа")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список товаров",
            content = @Content(schema = @Schema(implementation = CommonArtDTO.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    public ResponseEntity<?> getArts(@PathVariable String type) {
        List<CommonArtDTO> arts = artService.getAllArtsByType(type);
        return ResponseEntity.ok(arts);
    }

    @GetMapping("/search/paintings/object={input}")
    @Operation(summary = "Поиск картин по названию")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список картин",
            content = @Content(schema = @Schema(implementation = CommonArtDTO.class)))
    public ResponseEntity<?> searchPaintings(@PathVariable String input) {
        List<CommonArtDTO> arts = artService.searchPaintings(input);
        return ResponseEntity.ok(arts);
    }

    @GetMapping("/search/photos/object={input}")
    @Operation(summary = "Поиск фотографий по названию")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список фотографий",
            content = @Content(schema = @Schema(implementation = CommonArtDTO.class)))
    public ResponseEntity<?> searchPhotos(@PathVariable String input) {
        List<CommonArtDTO> arts = artService.searchPhotos(input);
        return ResponseEntity.ok(arts);
    }

    @GetMapping("/search/sculptures/object={input}")
    @Operation(summary = "Поиск скульптур по названию")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список скульптур",
            content = @Content(schema = @Schema(implementation = CommonArtDTO.class)))
    public ResponseEntity<?> searchSculptures(@PathVariable String input) {
        List<CommonArtDTO> arts = artService.searchSculptures(input);
        return ResponseEntity.ok(arts);
    }
}
