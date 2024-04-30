package ru.vsu.cs.sheina.online_gallery_backend.dto.order;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Data
@RequiredArgsConstructor
public class PurchaseDTO {

    Map<Integer, Boolean> arts;

    Integer cardId;

    Integer addressId;
}
