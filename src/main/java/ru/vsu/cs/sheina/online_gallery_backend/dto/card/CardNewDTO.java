package ru.vsu.cs.sheina.online_gallery_backend.dto.card;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;

@Data
@RequiredArgsConstructor
public class CardNewDTO {

    Integer cardId;

    String number;

    Timestamp date;

    Integer cvv;

    Boolean isDefault;
}
