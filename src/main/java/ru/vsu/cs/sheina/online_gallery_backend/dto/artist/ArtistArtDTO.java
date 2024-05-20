package ru.vsu.cs.sheina.online_gallery_backend.dto.artist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ArtistArtDTO {

    UUID artistId;

    String artistName;

    String avatarUrl;

    Integer viewsCount;

    Map<Integer, String> arts;
}
