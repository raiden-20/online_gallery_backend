package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vsu.cs.sheina.online_gallery_backend.entity.CardEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<CardEntity, Integer> {

    List<CardEntity> findAllByCustomerId(UUID customerId);

    Optional<CardEntity> findByCustomerIdAndIsDefault(UUID customerId, Boolean isDefault);

    Boolean existsByCustomerIdAndIsDefault(UUID customerId, Boolean isDefault);
}
