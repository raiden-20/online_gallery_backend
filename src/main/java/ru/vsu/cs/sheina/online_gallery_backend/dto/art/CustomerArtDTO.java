package ru.vsu.cs.sheina.online_gallery_backend.dto.art;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class CustomerArtDTO {

    Integer artId;

    String artistName;

    String name;

    String photoUrl;

    Double price;

    Boolean isPrivate;
}
