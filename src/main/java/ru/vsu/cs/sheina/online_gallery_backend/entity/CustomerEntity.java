package ru.vsu.cs.sheina.online_gallery_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "customer")
public class CustomerEntity {

    @Id
    @Column(name = "id")
    UUID id;

    @Column(name = "customer_name")
    String customerName;

    @Column(name = "gender")
    String gender;

    @Column(name = "birth_date")
    Timestamp birthDate;

    @Column(name = "avatar_url")
    String avatarUrl;

    @Column(name = "cover_url")
    String coverUrl;

    @Column(name = "description")
    String description;

    @Column(name = "artist_id")
    UUID artistId;
}
