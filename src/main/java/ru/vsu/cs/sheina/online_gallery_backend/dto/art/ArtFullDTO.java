package ru.vsu.cs.sheina.online_gallery_backend.dto.art;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class ArtFullDTO {

    String name;

    String type;

    List<String> photoUrls;

    Double price;

    UUID artistId;

    String status;

    Boolean available;

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
