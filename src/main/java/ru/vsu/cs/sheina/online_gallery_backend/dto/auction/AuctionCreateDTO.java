package ru.vsu.cs.sheina.online_gallery_backend.dto.auction;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;

@Data
@RequiredArgsConstructor
public class AuctionCreateDTO {

    String name;

    String type;

    BigInteger startPrice;

    String description;

    String size;

    Boolean frame;

    Timestamp createDate;

    List<String> tags;

    List<String> materials;

    Timestamp startDate;

    Timestamp endDate;
}
