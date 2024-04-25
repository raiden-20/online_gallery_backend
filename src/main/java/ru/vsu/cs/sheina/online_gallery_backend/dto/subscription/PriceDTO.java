package ru.vsu.cs.sheina.online_gallery_backend.dto.subscription;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class PriceDTO {

    UUID artistId;

    Double price;
}
