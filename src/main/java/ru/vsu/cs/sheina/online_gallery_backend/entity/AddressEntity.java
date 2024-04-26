package ru.vsu.cs.sheina.online_gallery_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "address")
public class AddressEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "customer_id")
    UUID customerId;

    @Column(name = "name")
    String name;

    @Column(name = "country")
    String country;

    @Column(name = "region")
    String region;

    @Column(name = "city")
    String city;

    @Column(name = "location")
    String location;

    @Column(name = "index")
    Integer index;

    @Column(name = "is_default")
    Boolean isDefault;
}
