package ru.vsu.cs.sheina.online_gallery_backend.dto.field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class UUIDRequestDTO{

    UUID id;
}
