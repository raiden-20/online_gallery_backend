package ru.vsu.cs.sheina.online_gallery_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "artist")
public class ArtistEntity {

    @Id
    @Column(name = "id")
    UUID id;

    @Column(name = "artist_name")
    String artistName;

    @Column(name = "avatar_url")
    String avatarUrl;

    @Column(name = "cover_url")
    String coverUrl;

    @Column(name = "views")
    Integer views;
}
