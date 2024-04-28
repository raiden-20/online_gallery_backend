package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.sheina.online_gallery_backend.entity.PostPhotoEntity;

import java.util.List;

public interface PostPhotoRepository extends JpaRepository<PostPhotoEntity, Integer> {

    @Transactional
    void deleteAllByPostIdAndAndPhotoUrl(Integer postId, String url);

    @Transactional
    void deleteAllByPostId(Integer postId);

    List<PostPhotoEntity> findAllByPostId(Integer postId);
}
