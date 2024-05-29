package ru.vsu.cs.sheina.online_gallery_backend.dto.auction;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;

@Data
@RequiredArgsConstructor
public class AuctionChangeDTO {

    Integer auctionId;

    String name;

    String type;

    List<String> deletePhotoUrls;

    Boolean changeMainPhoto;

    BigInteger startPrice;;

    Timestamp createDate;

    String description;

    String size;

    Boolean frame;

    List<String> tags;

    List<String> materials;

    Timestamp startDate;

    Timestamp endDate;
}
