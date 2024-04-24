package ru.vsu.cs.sheina.online_gallery_backend.dto.subscription;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class PrivateSubscriptionDTO {

    UUID artistId;

    String avatarUrl;

    String artistName;

    Double price;

    Timestamp payDate;
}
