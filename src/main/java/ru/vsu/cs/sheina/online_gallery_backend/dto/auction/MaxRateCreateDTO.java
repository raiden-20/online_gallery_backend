package ru.vsu.cs.sheina.online_gallery_backend.dto.auction;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;

@Data
@RequiredArgsConstructor
public class MaxRateCreateDTO {

    Integer auctionId;

    BigInteger maxRate;

    Boolean isAnonymous;
}
