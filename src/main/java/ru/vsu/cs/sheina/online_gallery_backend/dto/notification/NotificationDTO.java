package ru.vsu.cs.sheina.online_gallery_backend.dto.notification;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;

@Data
@RequiredArgsConstructor
public class NotificationDTO {

    Integer notificationId;

    Integer subjectId;

    NotificationType type;

    String text;

    Timestamp date;

    String avatarUrl;

    Boolean isSystem;
}
