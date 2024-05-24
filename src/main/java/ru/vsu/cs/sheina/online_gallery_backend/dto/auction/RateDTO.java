package ru.vsu.cs.sheina.online_gallery_backend.dto.auction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.UUID;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class RateDTO {

    UUID customerId;

    String customerName;

    String customerUrl;

    BigInteger rate;
}
