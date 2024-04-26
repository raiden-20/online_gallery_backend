package ru.vsu.cs.sheina.online_gallery_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "art")
public class ArtEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "name")
    String name;

    @Column(name = "type")
    String type;

    @Column(name = "price")
    Double price;

    @Column(name = "artist_id")
    UUID artistId;

    @Column(name = "owner_id")
    UUID ownerId;

    @Column(name = "sold")
    Boolean sold;

    @Column(name = "description")
    String description;

    @Column(name = "size")
    String size;

    @Column(name = "create_date")
    Timestamp createDate;

    @Column(name = "tags")
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    List<String> tags;

    @Column(name = "materials")
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    List<String> materials;

    @Column(name = "frame")
    Boolean frame;

    @Column(name = "publish_date")
    Timestamp publishDate;

    @Column(name = "views")
    Integer views;
}
