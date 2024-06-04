package ru.vsu.cs.sheina.online_gallery_backend.dto.customer;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FirstEntryDTO {

    Boolean firstEntry;

    Boolean isAdmin;
}
