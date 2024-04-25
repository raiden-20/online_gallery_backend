package ru.vsu.cs.sheina.online_gallery_backend.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "private_subscription")
public class PrivateSubscriptionEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "artist_id")
    UUID artistId;

    @Column(name = "price")
    Double price;

    @Column(name = "create_date")
    Timestamp createDate;
}
