package ru.vsu.cs.sheina.online_gallery_backend.dto.order;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.sql.Timestamp;

@Data
@RequiredArgsConstructor
public class OrderDTO {

    Integer orderId;

    String name;

    String country;

    String region;

    String city;

    Integer index;

    String location;

    String cardType;

    String number;

    String artistName;

    String customerName;

    String artUrl;

    String artName;

    BigInteger price;

    String status;

    String artistComment;

    Timestamp createDate;
}
