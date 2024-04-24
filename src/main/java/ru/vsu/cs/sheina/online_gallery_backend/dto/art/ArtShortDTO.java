package ru.vsu.cs.sheina.online_gallery_backend.dto.art;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class ArtShortDTO {

    Integer artId;

    String name;

    String photoUrl;

    Double price;

    UUID artistId;

    String artistName;
}
