package ru.vsu.cs.sheina.online_gallery_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ArtistShortDTO {

    UUID artistId;

    String artistName;

    String avatarUrl;

    Integer viewsCount;
}
