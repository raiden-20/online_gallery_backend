package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vsu.cs.sheina.online_gallery_backend.entity.OrderEntity;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, Integer> {

    List<OrderEntity> findAllByCustomerId(UUID customerId);

    List<OrderEntity> findAllByArtistId(UUID artistId);
}
