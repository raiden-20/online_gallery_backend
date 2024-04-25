package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vsu.cs.sheina.online_gallery_backend.entity.CustomerPrivateSubscriptionEntity;

public interface CustomerPrivateSubscriptionRepository extends JpaRepository<CustomerPrivateSubscriptionEntity, Integer> {
}
