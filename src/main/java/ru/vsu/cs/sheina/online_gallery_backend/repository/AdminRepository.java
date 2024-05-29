package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vsu.cs.sheina.online_gallery_backend.entity.AdminEntity;

import java.util.UUID;

public interface AdminRepository extends JpaRepository<AdminEntity, UUID> {
}
