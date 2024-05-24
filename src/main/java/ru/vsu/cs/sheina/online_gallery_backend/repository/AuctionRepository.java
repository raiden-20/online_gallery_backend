package ru.vsu.cs.sheina.online_gallery_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vsu.cs.sheina.online_gallery_backend.entity.AuctionEntity;

import java.util.List;
import java.util.UUID;

public interface AuctionRepository extends JpaRepository<AuctionEntity, Integer> {

    List<AuctionEntity> findAllByArtistId(UUID artistId);
}
