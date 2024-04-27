package ru.vsu.cs.sheina.online_gallery_backend.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

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

    Double salesAmount;

    Integer countSubscribers;

    Boolean isPublicSubscribe;

    Boolean isPrivateSubscribe;
}
