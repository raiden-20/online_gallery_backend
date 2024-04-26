package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vsu.cs.sheina.online_gallery_backend.entity.AddressEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<AddressEntity, Integer> {

    List<AddressEntity> findAllByCustomerId(UUID customerId);

    Optional<AddressEntity> findByCustomerIdAndIsDefault(UUID customerId, Boolean isDefault);

    Boolean existsByCustomerIdAndIsDefault(UUID customerId, Boolean isDefault);
}
