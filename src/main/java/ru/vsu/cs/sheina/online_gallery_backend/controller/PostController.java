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
import ru.vsu.cs.sheina.online_gallery_backend.dto.post.PostChangeDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.post.PostCreateDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.post.PostDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentialsException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.ForbiddenActionException;
import ru.vsu.cs.sheina.online_gallery_backend.service.PostService;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Блог")
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;

    @GetMapping("/{artistId}")
    @Operation(summary = "Получить посты художника")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список постов",
            content = @Content(schema = @Schema(implementation = PostDTO.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    @ApiResponse(responseCode = "403",
            description = "Действие запрещено",
            content = @Content(schema = @Schema(implementation = ForbiddenActionException.class)))
    public ResponseEntity<?> getPosts(@PathVariable UUID artistId,
                                      @RequestHeader("Authorization") String token) {
        List<PostDTO> posts = postService.getPosts(artistId, token);
        return ResponseEntity.ok(posts);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Создать пост")
    @ApiResponse(responseCode = "200",
            description = "Пост успешно создан",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    public ResponseEntity<?> createPost(@RequestPart(value = "photos") List<MultipartFile> photos,
                                        @RequestPart("PostCreateDTO") PostCreateDTO postCreateDTO,
                                        @RequestHeader("Authorization") String token) {
        postService.createPost(photos, postCreateDTO, token);
        return ResponseEntity.ok("Post created successfully");
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Изменить существующий пост")
    @ApiResponse(responseCode = "200",
            description = "Пост успешно изменен",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    @ApiResponse(responseCode = "403",
            description = "Действие запрещено",
            content = @Content(schema = @Schema(implementation = ForbiddenActionException.class)))
    public ResponseEntity<?> changePost(@RequestPart(value = "newPhotos") List<MultipartFile> newPhotos,
                                        @RequestPart("PostChangeDTO") PostChangeDTO postChangeDTO,
                                        @RequestHeader("Authorization") String token) {
        postService.changePost(newPhotos, postChangeDTO, token);
        return ResponseEntity.ok("Post changed successfully");
    }

    @DeleteMapping()
    @Operation(summary = "Удалить пост")
    @ApiResponse(responseCode = "200",
            description = "Пост успешно удален",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    @ApiResponse(responseCode = "403",
            description = "Действие запрещено",
            content = @Content(schema = @Schema(implementation = ForbiddenActionException.class)))
    public ResponseEntity<?> deletePost(@RequestBody IntIdRequestDTO intIdRequestDTO,
                                        @RequestHeader("Authorization") String token) {
        postService.delete(intIdRequestDTO, token);
        return ResponseEntity.ok("Post deleted successfully");
    }
}
