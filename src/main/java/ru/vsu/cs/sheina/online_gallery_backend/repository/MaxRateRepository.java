package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.sheina.online_gallery_backend.entity.MaxRateEntity;

import java.util.Optional;
import java.util.UUID;

public interface MaxRateRepository extends JpaRepository<MaxRateEntity, Integer> {

    Optional<MaxRateEntity> findByAuctionIdAndCustomerId(Integer auctionId, UUID customerId);

    Optional<MaxRateEntity> findByAuctionId(Integer auctionId);

    boolean existsByAuctionIdAndAndCustomerId(Integer auctionId, UUID customerId);

    @Transactional
    void deleteById(Integer id);

    @Transactional
    void deleteAllByAuctionId(Integer auctionId);
}
