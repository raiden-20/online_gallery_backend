package ru.vsu.cs.sheina.online_gallery_backend.dto.art;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class CommonArtDTO {

    Integer artId;

    String name;

    String photoUrl;

    BigInteger price;

    Boolean isPrivate;

    String size;

    Timestamp createDate;

    List<String> tags;

    List<String> materials;

    Boolean frame;

    Integer viewCount;

    UUID artistId;

    String artistName;

    String avatarUrl;

    UUID customerId;

    String customerName;
}
