package ru.vsu.cs.sheina.online_gallery_backend.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class CustomerRegistrationDTO {

    String customerName;

    Timestamp birthDate;

    String gender;
}
