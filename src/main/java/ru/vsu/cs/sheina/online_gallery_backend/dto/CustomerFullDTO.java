package ru.vsu.cs.sheina.online_gallery_backend.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.vsu.cs.sheina.online_gallery_backend.entity.enums.Gender;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class CustomerFullDTO {

    String customerName;

    Timestamp birthDate;

    Gender gender;

    String avatarUrl;

    String coverUrl;

    String description;

    UUID artistId;
}
