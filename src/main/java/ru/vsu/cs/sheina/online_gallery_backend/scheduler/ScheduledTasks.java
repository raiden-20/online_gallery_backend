package ru.vsu.cs.sheina.online_gallery_backend.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.vsu.cs.sheina.online_gallery_backend.entity.ArtistEntity;
import ru.vsu.cs.sheina.online_gallery_backend.entity.AuctionEntity;
import ru.vsu.cs.sheina.online_gallery_backend.entity.AuctionPhotoEntity;
import ru.vsu.cs.sheina.online_gallery_backend.entity.OrderEntity;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.UserNotFoundException;
import ru.vsu.cs.sheina.online_gallery_backend.repository.*;
import ru.vsu.cs.sheina.online_gallery_backend.service.AuctionService;
import ru.vsu.cs.sheina.online_gallery_backend.service.FileService;
import ru.vsu.cs.sheina.online_gallery_backend.service.NotificationService;

import java.sql.Timestamp;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final AuctionRepository auctionRepository;
    private final NotificationRepository notificationRepository;
    private final FileService fileService;
    private final MaxRateRepository maxRateRepository;
    private final RateRepository rateRepository;
    private final AuctionPhotoRepository auctionPhotoRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final AuctionService auctionService;
    private final ArtistRepository artistRepository;


    @Scheduled(fixedRate = 10000)
    private void doScheduledTasks() {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        changeAuctions(time);
        checkOrders(time);
    }

    private void changeAuctions(Timestamp time) {
        List<AuctionEntity> auctions = auctionRepository.findAll();

        for (AuctionEntity auctionEntity: auctions) {
            if (auctionEntity.getStartDate().before(time)) {
                auctionEntity.setStatus("AVAILABLE");
                auctionRepository.save(auctionEntity);

                notificationService.sendStartPublicAuctionNotification(auctionEntity);
            }

            if (auctionEntity.getEndDate().before(time)) {
                auctionService.finishAuction(auctionEntity);
            }
        }
    }


    private void checkOrders(Timestamp time) {
        List<OrderEntity> orderEntities = orderRepository.findAll();

        for (OrderEntity orderEntity: orderEntities) {
            if (orderEntity.getStatus().equals("AWAIT") && orderEntity.getCreateDate().before(time)) {
                ArtistEntity artistEntity = artistRepository.findById(orderEntity.getArtistId()).orElseThrow(UserNotFoundException::new);
                orderRepository.deleteAllBySubjectId(orderEntity.getSubjectId());

                notificationRepository.deleteAllBySubjectId(orderEntity.getSubjectId());
                orderRepository.deleteAllBySubjectId(orderEntity.getSubjectId());

                auctionPhotoRepository.findAllByAuctionId(orderEntity.getSubjectId()).stream()
                        .map(AuctionPhotoEntity::getPhotoUrl)
                        .forEach(fileService::deleteFile);

                auctionPhotoRepository.deleteAllByAuctionId(orderEntity.getSubjectId());
                maxRateRepository.deleteAllByAuctionId(orderEntity.getSubjectId());
                rateRepository.deleteAllByAuctionId(orderEntity.getSubjectId());
                auctionRepository.deleteById(orderEntity.getSubjectId());

                //TODO блок пользователя

                notificationService.sendAuctionFailedNotification(orderEntity.getCustomerId(), artistEntity);
            }
        }
    }
}
