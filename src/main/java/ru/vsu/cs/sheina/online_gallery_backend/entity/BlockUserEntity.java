package ru.vsu.cs.sheina.online_gallery_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "block_user")
public class BlockUserEntity {

    @Id
    @Column(name = "id")
    UUID id;
}
