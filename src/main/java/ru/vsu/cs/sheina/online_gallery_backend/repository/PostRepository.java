package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.sheina.online_gallery_backend.entity.PostEntity;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<PostEntity, Integer> {

    List<PostEntity> findAllByArtistId(UUID aristId);

    @Transactional
    void deleteAllByArtistId(UUID artistId);
}
