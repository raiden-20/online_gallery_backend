package ru.vsu.cs.sheina.online_gallery_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class CustomerShortDTO {

    UUID customerId;

    String customerName;

    String avatarUrl;
}
