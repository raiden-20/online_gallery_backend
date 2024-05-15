package ru.vsu.cs.sheina.online_gallery_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "notification")
public class NotificationEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "receiver_id")
    UUID receiverId;

    @Column(name = "sender_id")
    UUID senderId;

    @Column(name = "type")
    String type;

    @Column(name = "text")
    String text;

    @Column(name = "subject_id")
    Integer subjectId;

    @Column(name = "create_date")
    Timestamp createDate;
}
