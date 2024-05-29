package ru.vsu.cs.sheina.online_gallery_backend.dto.art;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class ArtFullDTO {

    String name;

    String type;

    List<String> photoUrls;

    BigInteger price;

    Boolean isPrivate;

    UUID artistId;

    String artistName;

    Integer eventId;

    String eventName;

    String status;

    UUID customerId;

    String customerName;

    String description;

    String size;

    Timestamp createDate;

    List<String> tags;

    List<String> materials;

    Boolean frame;

    Timestamp publishDate;
}
