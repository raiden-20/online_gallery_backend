package ru.vsu.cs.sheina.online_gallery_backend.dto.subscription;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class SubscribeDTO {

    UUID artistId;

    Integer cardId;
}
