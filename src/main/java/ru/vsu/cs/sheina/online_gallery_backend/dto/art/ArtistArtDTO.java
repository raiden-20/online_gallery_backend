package ru.vsu.cs.sheina.online_gallery_backend.dto.art;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class ArtistArtDTO {

    Integer artId;

    String name;

    String photoUrl;

    BigInteger price;

    Boolean isPrivate;

    Boolean available;

    UUID customerId;

    String customerName;

    String avatarUrl;
}

