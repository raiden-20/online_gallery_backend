package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vsu.cs.sheina.online_gallery_backend.entity.NotificationEntity;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Integer> {

    List<NotificationEntity> findAllByReceiverId(UUID receiverId);

    void deleteAllBySubjectId(Integer id);

    void deleteAllBySenderId(UUID senderId);

    void deleteAllByReceiverId(UUID receiverId);
}
