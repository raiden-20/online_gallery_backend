package ru.vsu.cs.sheina.online_gallery_backend.dto.post;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PostCreateDTO {

    String title;

    String text;
}
