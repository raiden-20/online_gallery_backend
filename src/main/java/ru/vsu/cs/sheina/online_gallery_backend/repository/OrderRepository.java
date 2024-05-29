package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.sheina.online_gallery_backend.entity.OrderEntity;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, Integer> {

    List<OrderEntity> findAllByCustomerId(UUID customerId);

    List<OrderEntity> findAllByCardId(Integer cardId);

    List<OrderEntity> findAllByAddressId(Integer addressId);

    List<OrderEntity> findAllByArtistId(UUID artistId);

    @Transactional
    void deleteAllBySubjectId(Integer subjectId);

    @Transactional
    void deleteAllByCustomerId(UUID customerId);
}
