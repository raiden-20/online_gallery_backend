package ru.vsu.cs.sheina.online_gallery_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.sheina.online_gallery_backend.dto.PostDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;

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
    public ResponseEntity<?> createPost(@RequestPart(value = "files") List<MultipartFile> photos,
                                        @RequestPart("title") String title,
                                        @RequestPart("text") String text,
                                        @RequestHeader("Authorization") String token) {
        postService.createPost(photos, title, text, token);
        return ResponseEntity.ok("Post created successfully");
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPost(@RequestPart(value = "files") List<MultipartFile> photos,
                                        @RequestPart("deletePhotoUrls") List<String> deletePhotoUrls,
                                        @RequestPart("postId") String postId,
                                        @RequestPart("title") String title,
                                        @RequestPart("text") String text,
                                        @RequestHeader("Authorization") String token) {
        postService.changePost(photos, deletePhotoUrls, postId, title, text, token);
        return ResponseEntity.ok("Post changed successfully");
    }

    @DeleteMapping()
    public ResponseEntity<?> deletePost(@RequestBody IntIdRequestDTO intIdRequestDTO,
                                        @RequestHeader("Authorization") String token) {
        postService.delete(intIdRequestDTO, token);
        return ResponseEntity.ok("Post deleted successfully");
    }
}
