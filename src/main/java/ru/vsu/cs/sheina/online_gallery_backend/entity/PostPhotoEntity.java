package ru.vsu.cs.sheina.online_gallery_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "post_photo")
public class PostPhotoEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "post_id")
    Integer postId;

    @Column(name = "photo_url")
    String photoUrl;

    @Column(name = "default_photo")
    Boolean defaultPhoto;
}
