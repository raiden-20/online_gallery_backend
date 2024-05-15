package ru.vsu.cs.sheina.online_gallery_backend.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class NotificationShortDTO {

    String avatarUrl;

    String message;
}
