package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vsu.cs.sheina.online_gallery_backend.entity.ArtEntity;

import java.util.List;
import java.util.UUID;

public interface ArtRepository extends JpaRepository<ArtEntity, Integer> {

    List<ArtEntity> findAllByType(String type);

    List<ArtEntity> findAllByArtistId(UUID artistId);

    List<ArtEntity> findAllByOwnerId(UUID customerId);
}
