package ru.vsu.cs.sheina.online_gallery_backend.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class ArtistRegistrationDTO {

    UUID customerId;

    String artistName;
}
