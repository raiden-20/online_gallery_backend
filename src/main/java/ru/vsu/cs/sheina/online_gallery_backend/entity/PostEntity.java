package ru.vsu.cs.sheina.online_gallery_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "post")
public class PostEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "artist_id")
    UUID artistId;

    @Column(name = "title")
    String title;

    @Column(name = "body")
    String body;

    @Column(name = "created_at")
    Timestamp createdAt;
}
