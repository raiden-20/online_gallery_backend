package ru.vsu.cs.sheina.online_gallery_backend.dto.auction;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RateCreateDTO {

    Integer auctionId;

    Boolean isAnonymous;
}
