package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.sheina.online_gallery_backend.entity.BlockUserEntity;

import java.util.UUID;

public interface BlockUserRepository extends JpaRepository<BlockUserEntity, UUID> {

    @Transactional
    void deleteAllById(UUID id);
}
