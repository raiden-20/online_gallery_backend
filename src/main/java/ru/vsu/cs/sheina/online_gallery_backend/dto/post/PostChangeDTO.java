package ru.vsu.cs.sheina.online_gallery_backend.dto.post;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class PostChangeDTO {

    Integer postId;

    String title;

    String text;

    List<String> deletePhotoUrls;
}
