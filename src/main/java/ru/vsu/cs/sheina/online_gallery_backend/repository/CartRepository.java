package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.sheina.online_gallery_backend.entity.CartEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<CartEntity, Integer> {

    Boolean existsByCustomerIdAndSubjectId(UUID customerId, Integer subjectId);

    Optional<CartEntity> findByCustomerIdAndSubjectId(UUID customerId, Integer subjectId);

    @Transactional
    void deleteAllBySubjectId(Integer subjectId);

    @Transactional
    void deleteAllByCustomerId(UUID customerId);

    List<CartEntity> findAllByCustomerId(UUID customerId);
}
