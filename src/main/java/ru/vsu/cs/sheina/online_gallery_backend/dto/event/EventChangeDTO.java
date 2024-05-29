package ru.vsu.cs.sheina.online_gallery_backend.dto.event;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;

@Data
@RequiredArgsConstructor
public class EventChangeDTO {

    Integer eventId;

    String name;

    Boolean changeMainPhoto;

    String description;

    Timestamp startDate;

    Timestamp endDate;
}
