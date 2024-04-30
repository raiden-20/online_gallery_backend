package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vsu.cs.sheina.online_gallery_backend.entity.PrivateSubscriptionEntity;

import java.util.Optional;
import java.util.UUID;

public interface PrivateSubscriptionRepository extends JpaRepository<PrivateSubscriptionEntity, Integer> {

    boolean existsByArtistId(UUID artistId);

    Optional<PrivateSubscriptionEntity> findByArtistId(UUID artistId);
}
