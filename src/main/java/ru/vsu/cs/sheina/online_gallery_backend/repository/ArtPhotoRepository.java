package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.sheina.online_gallery_backend.entity.ArtPhotoEntity;

import java.util.List;
import java.util.Optional;

public interface ArtPhotoRepository extends JpaRepository<ArtPhotoEntity, Integer> {

    List<ArtPhotoEntity> findAllByArtId(Integer artId);

    @Transactional
    void deleteAllByArtId(Integer artId);

    @Transactional
    void deleteAllByArtIdAndAndPhotoUrl(Integer artId, String url);

    Optional<ArtPhotoEntity> findByArtIdAndAndDefaultPhoto(Integer artId, Boolean isDefaultPhoto);
}
