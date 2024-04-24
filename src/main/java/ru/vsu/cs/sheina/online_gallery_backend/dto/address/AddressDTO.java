package ru.vsu.cs.sheina.online_gallery_backend.dto.address;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class AddressDTO {

    Integer addressId;

    String name;

    String country;

    String region;

    String city;

    Integer index;

    String location;

    Boolean isDefault;
}
