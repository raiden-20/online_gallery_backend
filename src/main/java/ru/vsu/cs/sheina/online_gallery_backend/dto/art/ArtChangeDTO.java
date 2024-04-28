package ru.vsu.cs.sheina.online_gallery_backend.dto.art;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class ArtChangeDTO {

    Integer artId;

    String name;

    String type;

    List<String> deletePhotoUrls;

    Boolean changeMainPhoto;

    Boolean isPrivate;

    Double price;

    String description;

    String size;

    Boolean frame;

    List<String> tags;

    List<String> materials;
}
