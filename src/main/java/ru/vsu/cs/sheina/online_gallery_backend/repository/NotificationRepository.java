package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.sheina.online_gallery_backend.entity.NotificationEntity;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Integer> {

    List<NotificationEntity> findAllByReceiverId(UUID receiverId);

    @Transactional
    void deleteAllBySubjectId(Integer id);

    @Transactional
    void deleteAllBySenderId(UUID senderId);

    @Transactional
    void deleteAllByReceiverId(UUID receiverId);
}
