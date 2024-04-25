package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vsu.cs.sheina.online_gallery_backend.entity.CustomerPrivateSubscriptionEntity;

import java.util.UUID;
import java.util.List;

public interface CustomerPrivateSubscriptionRepository extends JpaRepository<CustomerPrivateSubscriptionEntity, Integer> {

    boolean existsByCustomerIdAndPrivateSubscriptionId(UUID customerId, Integer subscriptionId);

    void deleteAllByCustomerIdAndPrivateSubscriptionId(UUID customerId, Integer subscriptionId);

    void deleteAllByPrivateSubscriptionId(Integer subscriptionId);

    List<CustomerPrivateSubscriptionEntity> findAllByPrivateSubscriptionId(Integer subscriptionId);

    List<CustomerPrivateSubscriptionEntity> findAllByCustomerId(UUID customerId);
}
