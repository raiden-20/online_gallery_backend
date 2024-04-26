package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vsu.cs.sheina.online_gallery_backend.entity.ArtPrivateSubscriptionEntity;

import java.util.Optional;

public interface ArtPrivateSubscriptionRepository extends JpaRepository<ArtPrivateSubscriptionEntity, Integer> {

    Boolean existsByArtId(Integer artId);

    void deleteAllByArtId(Integer artId);

    void deleteAllBySubscriptionId(Integer subscriptionId);

    Optional<ArtPrivateSubscriptionEntity> findByArtId(Integer artId);
}
