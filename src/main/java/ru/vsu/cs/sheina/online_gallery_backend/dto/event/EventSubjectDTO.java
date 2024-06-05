package ru.vsu.cs.sheina.online_gallery_backend.dto.event;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class EventSubjectDTO {

    Integer subjectId;

    UUID artistId;

    String artistName;

    String subjectName;

    String photoUrl;

    String status;

    BigInteger price;

    UUID customerId;

    String customerUrl;

    String customerName;

    Integer viewCount;

    String size;

    Timestamp startDate;

    Timestamp endDate;

    Timestamp createDate;

    List<String> tags;

    List<String> materials;

    Boolean frame;
}
