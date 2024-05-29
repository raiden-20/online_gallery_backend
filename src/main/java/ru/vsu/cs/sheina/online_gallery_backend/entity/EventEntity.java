package ru.vsu.cs.sheina.online_gallery_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@Table(name = "event")
public class EventEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "name")
    String name;

    @Column(name = "photo_url")
    String photoUrl;

    @Column(name = "description")
    String description;

    @Column(name = "views")
    Integer views;

    @Column(name = "start_date")
    Timestamp startDate;

    @Column(name = "end_date")
    Timestamp endDate;
}
