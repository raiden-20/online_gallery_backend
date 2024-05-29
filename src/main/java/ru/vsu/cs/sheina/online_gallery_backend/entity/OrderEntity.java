package ru.vsu.cs.sheina.online_gallery_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "order_")
public class OrderEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "customer_id")
    UUID customerId;

    @Column(name = "artist_id")
    UUID artistId;

    @Column(name = "subject_id")
    Integer subjectId;

    @Column(name = "status")
    String status;

    @Column(name = "type")
    String type;

    @Column(name = "artist_comment")
    String artistComment;

    @Column(name = "cardId")
    Integer cardId;

    @Column(name = "address_id")
    Integer addressId;

    @Column(name = "create_date")
    Timestamp createDate;
}
