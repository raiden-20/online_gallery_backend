package ru.vsu.cs.sheina.online_gallery_backend.dto.artist;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class ArtistRegistrationDTO {

    String artistName;
}
