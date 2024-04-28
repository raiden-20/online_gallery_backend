package ru.vsu.cs.sheina.online_gallery_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.sheina.online_gallery_backend.dto.post.PostChangeDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.post.PostCreateDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.post.PostDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.service.PostService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {

    private final PostService postService;

    @GetMapping("/{artistId}")
    public ResponseEntity<?> getPosts(@PathVariable UUID artistId,
                                      @RequestHeader("Authorization") String token) {
        List<PostDTO> posts = postService.getPosts(artistId, token);
        return ResponseEntity.ok(posts);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPost(@RequestPart(value = "photos") List<MultipartFile> photos,
                                        @RequestPart("PostCreateDTO") PostCreateDTO postCreateDTO,
                                        @RequestHeader("Authorization") String token) {
        postService.createPost(photos, postCreateDTO, token);
        return ResponseEntity.ok("Post created successfully");
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> changePost(@RequestPart(value = "newPhotos") List<MultipartFile> newPhotos,
                                        @RequestPart("PostChangeDTO") PostChangeDTO postChangeDTO,
                                        @RequestHeader("Authorization") String token) {
        postService.changePost(newPhotos, postChangeDTO, token);
        return ResponseEntity.ok("Post changed successfully");
    }

    @DeleteMapping()
    public ResponseEntity<?> deletePost(@RequestBody IntIdRequestDTO intIdRequestDTO,
                                        @RequestHeader("Authorization") String token) {
        postService.delete(intIdRequestDTO, token);
        return ResponseEntity.ok("Post deleted successfully");
    }
}
