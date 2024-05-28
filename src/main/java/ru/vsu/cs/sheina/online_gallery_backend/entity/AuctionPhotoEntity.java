package ru.vsu.cs.sheina.online_gallery_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "auction_photo")
public class AuctionPhotoEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "auction_id")
    Integer auctionId;

    @Column(name = "photo_url")
    String photoUrl;

    @Column(name = "default_photo")
    Boolean defaultPhoto;
}
