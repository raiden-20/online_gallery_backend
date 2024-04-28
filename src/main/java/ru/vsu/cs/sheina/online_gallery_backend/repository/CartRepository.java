package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.sheina.online_gallery_backend.entity.CartEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<CartEntity, Integer> {

    Boolean existsByCustomerIdAndArtId(UUID customerId, Integer artId);

    Optional<CartEntity> findByCustomerIdAndArtId(UUID customerId, Integer artId);

    @Transactional
    void deleteAllByArtId(Integer artId);
    List<CartEntity> findAllByCustomerId(UUID customerId);
}
