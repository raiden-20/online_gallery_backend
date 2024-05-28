package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.sheina.online_gallery_backend.entity.RateEntity;

import java.math.BigInteger;
import java.util.*;

public interface RateRepository extends JpaRepository<RateEntity, Integer> {

    List<RateEntity> findAllByAuctionId(Integer auctionId);

    Integer countByAuctionId(Integer auctionId);

    Optional<RateEntity> findByRate(BigInteger rate);

    Boolean existsByAuctionId(Integer auctionId);

    @Transactional
    void deleteAllByAuctionId(Integer auctionId);
}
