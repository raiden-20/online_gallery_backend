package ru.vsu.cs.sheina.online_gallery_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "card")
public class CardEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "customer_id")
    UUID customerId;

    @Column(name = "number")
    String number;

    @Column(name = "date")
    Timestamp date;

    @Column(name = "cvv")
    Integer cvv;

    @Column(name = "is_default")
    Boolean isDefault;
}
