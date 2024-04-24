package ru.vsu.cs.sheina.online_gallery_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
public class PostDTO {

    Integer postId;

    String title;

    String text;

    List<String> photoUrls;

    Timestamp date;
}
