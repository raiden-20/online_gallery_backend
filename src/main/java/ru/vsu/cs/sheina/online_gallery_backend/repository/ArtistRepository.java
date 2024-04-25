package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vsu.cs.sheina.online_gallery_backend.entity.ArtistEntity;

import java.util.UUID;

public interface ArtistRepository extends JpaRepository<ArtistEntity, UUID> {

    boolean existsById(UUID artistId);
}
