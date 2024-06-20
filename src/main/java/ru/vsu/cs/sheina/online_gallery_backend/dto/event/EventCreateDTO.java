package ru.vsu.cs.sheina.online_gallery_backend.dto.event;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;

@Data
@RequiredArgsConstructor
public class EventCreateDTO {

    String name;

    String type;

    String description;

    Timestamp startDate;

    Timestamp endDate;
}
