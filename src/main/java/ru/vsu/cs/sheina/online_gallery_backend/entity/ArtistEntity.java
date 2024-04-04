package ru.vsu.cs.sheina.online_gallery_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
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

    @Column(name = "description")
    String description;


    @Column(name = "views")
    Integer views;
}
