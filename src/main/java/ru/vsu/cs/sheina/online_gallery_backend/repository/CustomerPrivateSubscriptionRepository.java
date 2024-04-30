package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.sheina.online_gallery_backend.entity.CustomerPrivateSubscriptionEntity;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface CustomerPrivateSubscriptionRepository extends JpaRepository<CustomerPrivateSubscriptionEntity, Integer> {

    boolean existsByCustomerIdAndPrivateSubscriptionId(UUID customerId, Integer subscriptionId);

    Integer countByPrivateSubscriptionId(Integer id);

    @Transactional
    void deleteAllByCardId(Integer cardId);

    @Transactional
    void deleteByCustomerIdAndPrivateSubscriptionId(UUID customerId, Integer subscriptionId);

    @Transactional
    void deleteAllByPrivateSubscriptionId(Integer subscriptionId);

    List<CustomerPrivateSubscriptionEntity> findAllByPrivateSubscriptionId(Integer subscriptionId);

    List<CustomerPrivateSubscriptionEntity> findAllByCustomerId(UUID customerId);
}
