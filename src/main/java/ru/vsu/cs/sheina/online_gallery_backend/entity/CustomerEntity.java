package ru.vsu.cs.sheina.online_gallery_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import ru.vsu.cs.sheina.online_gallery_backend.entity.enums.Gender;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Table(name = "customer")
public class CustomerEntity {

    @Id
    @Column(name = "id")
    UUID id;

    @Column(name = "customer_name")
    String customerName;

    @Column(name = "gender")
    Gender gender;

    @Column(name = "birth_date")
    Timestamp birthDate;

    @Column(name = "avatar_url")
    String avatarUrl;

    @Column(name = "cover_url")
    String coverUrl;

    @Column(name = "artist_url")
    UUID artistId;
}
