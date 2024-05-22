package ru.vsu.cs.sheina.online_gallery_backend.dto.auction;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class AuctionFullDTO {

    Integer auctionId;

    String name;

    List<String> photoUrls;

    String type;

    BigInteger lastPrice;

    UUID artistId;

    String artistName;

    String status;

    UUID customerId;

    String customerName;

    String customerUrl;

    Integer viewCount;

    String description;

    String size;

    Boolean frame;

    Timestamp createDate;

    List<String> tags;

    List<String> materials;

    BigInteger rate;

    Timestamp startDate;

    Timestamp endDate;

    List<RateDTO> customerRates;
}
