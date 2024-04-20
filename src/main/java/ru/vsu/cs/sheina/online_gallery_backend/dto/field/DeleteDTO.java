package ru.vsu.cs.sheina.online_gallery_backend.dto.field;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class DeleteDTO {

    UUID id;
}
