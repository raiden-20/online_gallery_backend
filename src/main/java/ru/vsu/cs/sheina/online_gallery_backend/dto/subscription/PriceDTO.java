package ru.vsu.cs.sheina.online_gallery_backend.dto.subscription;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class PriceDTO {

    UUID artistId;

    Double price;
}
