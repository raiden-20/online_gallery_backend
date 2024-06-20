package ru.vsu.cs.sheina.online_gallery_backend.dto.event;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;

@Data
@RequiredArgsConstructor
public class EventShortDTO {

    Integer eventId;

    String photoUrl;

    String bannerUrl;

    String name;

    Timestamp startDate;

    Timestamp endDate;

    String description;

    String status;
}
