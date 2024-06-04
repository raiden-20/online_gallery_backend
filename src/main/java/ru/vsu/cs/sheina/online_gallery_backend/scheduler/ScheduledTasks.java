package ru.vsu.cs.sheina.online_gallery_backend.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.UUIDRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.entity.*;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.UserNotFoundException;
import ru.vsu.cs.sheina.online_gallery_backend.repository.*;
import ru.vsu.cs.sheina.online_gallery_backend.service.*;

import java.sql.Timestamp;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final AuctionRepository auctionRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;
    private final NotificationRepository notificationRepository;
    private final FileService fileService;
    private final MaxRateRepository maxRateRepository;
    private final RateRepository rateRepository;
    private final AuctionPhotoRepository auctionPhotoRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final AuctionService auctionService;
    private final ArtistRepository artistRepository;
    private final KeycloakService keycloakService;


    @Scheduled(fixedRate = 10000)
    private void doScheduledTasks() {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        changeEvents(time);
        changeAuctions(time);
        checkOrders(time);
    }

    private void changeAuctions(Timestamp time) {
        List<AuctionEntity> auctions = auctionRepository.findAll();

        for (AuctionEntity auctionEntity: auctions) {
            if (auctionEntity.getStartDate().before(time) && auctionEntity.getStatus().equals("WAIT")) {
                auctionEntity.setStatus("AVAILABLE");
                auctionRepository.save(auctionEntity);

                notificationService.sendStartPublicAuctionNotification(auctionEntity);
            }

            if (auctionEntity.getEndDate().before(time) && auctionEntity.getStatus().equals("AVAILABLE")) {
                auctionService.finishAuction(auctionEntity);
            }
        }
    }


    private void checkOrders(Timestamp time) {
        List<OrderEntity> orderEntities = orderRepository.findAll();

        for (OrderEntity orderEntity: orderEntities) {
            Timestamp endOrderDate = new Timestamp(orderEntity.getCreateDate().getTime() + 24 * 60 * 60 * 1000);
            if (orderEntity.getStatus().equals("AWAIT") && endOrderDate.before(time)) {
                ArtistEntity artistEntity = artistRepository.findById(orderEntity.getArtistId()).orElseThrow(UserNotFoundException::new);

                notificationRepository.deleteAllBySubjectId(orderEntity.getSubjectId());

                auctionPhotoRepository.findAllByAuctionId(orderEntity.getSubjectId()).stream()
                        .map(AuctionPhotoEntity::getPhotoUrl)
                        .forEach(fileService::deleteFile);

                auctionPhotoRepository.deleteAllByAuctionId(orderEntity.getSubjectId());
                maxRateRepository.deleteAllByAuctionId(orderEntity.getSubjectId());
                rateRepository.deleteAllByAuctionId(orderEntity.getSubjectId());
                auctionRepository.deleteById(orderEntity.getSubjectId());

                keycloakService.blockUser(new UUIDRequestDTO(orderEntity.getCustomerId()));
                notificationService.sendAuctionFailedNotification(orderEntity.getCustomerId(), artistEntity);

                orderRepository.deleteAllBySubjectId(orderEntity.getSubjectId());
            }
        }
    }

    private void changeEvents(Timestamp time) {
        List<EventEntity> events = eventRepository.findAll();

        for (EventEntity eventEntity: events) {
            if (eventEntity.getStartDate().before(time) && eventEntity.getStatus().equals("WAIT")) {
                eventService.startEvent(eventEntity);
            }

            if (eventEntity.getEndDate().before(time) && eventEntity.getStatus().equals("AVAILABLE")) {
                eventService.finishEvent(eventEntity);
            }
        }
    }
}
