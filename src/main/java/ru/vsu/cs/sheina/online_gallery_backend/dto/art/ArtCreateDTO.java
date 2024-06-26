package ru.vsu.cs.sheina.online_gallery_backend.dto.art;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;

@Data
@RequiredArgsConstructor
public class ArtCreateDTO {

    String name;

    String type;

    Boolean isPrivate;

    Integer eventId;

    BigInteger price;

    String description;

    String size;

    Boolean frame;

    Timestamp createDate;

    List<String> tags;

    List<String> materials;
}
