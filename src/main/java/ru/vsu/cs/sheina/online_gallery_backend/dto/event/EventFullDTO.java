package ru.vsu.cs.sheina.online_gallery_backend.dto.event;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@RequiredArgsConstructor
public class EventFullDTO {

    String name;

    String photoUrl;

    String bannerUrl;

    String status;

    Timestamp startDate;

    Timestamp endDate;

    String description;

    List<EventSubjectDTO> subjects;

    String type;
}
