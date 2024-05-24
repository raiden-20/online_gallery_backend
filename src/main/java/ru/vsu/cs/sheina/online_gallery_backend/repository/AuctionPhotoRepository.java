package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.sheina.online_gallery_backend.entity.AuctionPhotoEntity;

import java.util.List;
import java.util.Optional;

public interface AuctionPhotoRepository extends JpaRepository<AuctionPhotoEntity, Integer> {

    void deleteAllByAuctionIdAndPhotoUrl(Integer auctionId, String url);

    List<AuctionPhotoEntity> findAllByAuctionId(Integer auctionId);

    Optional<AuctionPhotoEntity> findByAuctionIdAndDefaultPhoto(Integer auctionId, Boolean defaultPhoto);

    @Transactional
    void deleteAllByAuctionId(Integer auctionId);
}