package ru.vsu.cs.sheina.online_gallery_backend.dto.order;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ChangeOrderDTO {

    Integer orderId;

    Integer cardId;

    Integer addressId;
}
