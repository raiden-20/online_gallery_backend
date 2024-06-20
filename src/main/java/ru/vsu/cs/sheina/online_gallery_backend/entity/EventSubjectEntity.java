package ru.vsu.cs.sheina.online_gallery_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "event_subject")
public class EventSubjectEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "event_id")
    Integer eventId;

    @Column(name = "subject_id")
    Integer subjectId;
}
