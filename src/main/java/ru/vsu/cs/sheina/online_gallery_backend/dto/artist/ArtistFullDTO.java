package ru.vsu.cs.sheina.online_gallery_backend.dto.artist;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class ArtistFullDTO {

    String artistName;

    String avatarUrl;

    String coverUrl;

    UUID customerId;

    String description;

    Integer countSoldArts;

    BigInteger salesAmount;

    Integer countSubscribers;

    Boolean isPublicSubscribe;

    Boolean isPrivateSubscribe;
}
