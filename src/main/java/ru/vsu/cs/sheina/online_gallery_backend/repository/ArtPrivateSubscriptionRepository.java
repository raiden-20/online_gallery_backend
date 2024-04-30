package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.sheina.online_gallery_backend.entity.ArtPrivateSubscriptionEntity;

import java.util.Optional;

public interface ArtPrivateSubscriptionRepository extends JpaRepository<ArtPrivateSubscriptionEntity, Integer> {

    Boolean existsByArtId(Integer artId);

    @Transactional
    void deleteAllByArtId(Integer artId);

    @Transactional
    void deleteAllBySubscriptionId(Integer subscriptionId);

    Optional<ArtPrivateSubscriptionEntity> findByArtId(Integer artId);
}
