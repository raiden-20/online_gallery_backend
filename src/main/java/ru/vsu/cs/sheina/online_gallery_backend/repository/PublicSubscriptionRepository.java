package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.sheina.online_gallery_backend.entity.PublicSubscriptionEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PublicSubscriptionRepository extends JpaRepository<PublicSubscriptionEntity, Integer> {

    Optional<PublicSubscriptionEntity> findByArtistIdAndCustomerId(UUID artistId, UUID customerId);

    List<PublicSubscriptionEntity> findAllByCustomerId(UUID customerId);

    List<PublicSubscriptionEntity> findAllByArtistId(UUID artistId);

    @Transactional
    void deleteById(Integer id);

    @Transactional
    void deleteAllByCustomerId(UUID customerId);

    @Transactional
    void deleteAllByArtistId(UUID artistId);

    Boolean existsByArtistIdAndCustomerId(UUID artistId, UUID customerId);
}
