package ru.vsu.cs.sheina.online_gallery_backend.dto.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class CardDTO {

    Integer cardId;

    String type;

    String number;

    Timestamp date;

    Integer cvv;

    Boolean isDefault;
}
