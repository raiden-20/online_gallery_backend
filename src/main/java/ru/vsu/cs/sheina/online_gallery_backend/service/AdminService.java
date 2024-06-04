package ru.vsu.cs.sheina.online_gallery_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.sheina.online_gallery_backend.dto.event.EventChangeDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.event.EventCreateDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.entity.*;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadActionException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentialsException;
import ru.vsu.cs.sheina.online_gallery_backend.repository.*;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final NotificationService notificationService;
    private final AdminRepository adminRepository;
    private final NotificationRepository notificationRepository;
    private final CartRepository cartRepository;
    private final ArtRepository artRepository;
    private final ArtPhotoRepository artPhotoRepository;
    private final ArtPrivateSubscriptionRepository artPrivateSubscriptionRepository;
    private final FileService fileService;
    private final AuctionRepository auctionRepository;
    private final AuctionPhotoRepository auctionPhotoRepository;
    private final MaxRateRepository maxRateRepository;
    private final RateRepository rateRepository;
    private final EventRepository eventRepository;
    private final EventSubjectRepository eventSubjectRepository;

    public void deleteArt(IntIdRequestDTO intIdRequestDTO) {
        ArtEntity artEntity = artRepository.findById(intIdRequestDTO.getId()).orElseThrow(BadCredentialsException::new);

        if (artEntity.getSold()) {
            throw new BadActionException("You cannot delete purchased paintings");
        }

        UUID artistId = artEntity.getArtistId();
        String artName = artEntity.getName();

        notificationRepository.deleteAllBySubjectId(artEntity.getId());
        cartRepository.deleteAllBySubjectId(artEntity.getId());
        artPrivateSubscriptionRepository.deleteAllByArtId(artEntity.getId());

        artPhotoRepository.findAllByArtId(artEntity.getId()).stream()
                .map(ArtPhotoEntity::getPhotoUrl)
                .forEach(fileService::deleteFile);

        artPhotoRepository.deleteAllByArtId(artEntity.getId());
        artRepository.deleteById(artEntity.getId());

        notificationService.sendArtBlockNotification(artistId, artName);
    }

    public void deleteAuction(IntIdRequestDTO intIdRequestDTO) {
        AuctionEntity auctionEntity = auctionRepository.findById(intIdRequestDTO.getId()).orElseThrow(BadCredentialsException::new);

        if (auctionEntity.getStatus().equals("SOLD")) {
            throw new BadActionException("You cannot delete purchased auction");
        }

        UUID artistId = auctionEntity.getArtistId();
        String auctionName = auctionEntity.getName();

        maxRateRepository.deleteAllByAuctionId(intIdRequestDTO.getId());
        rateRepository.deleteAllByAuctionId(intIdRequestDTO.getId());
        auctionPhotoRepository.deleteAllByAuctionId(auctionEntity.getId());
        auctionRepository.delete(auctionEntity);

        notificationService.sendAuctionBlockNotification(artistId, auctionName);
    }

    public Integer createEvent(EventCreateDTO eventCreateDTO, MultipartFile photo, MultipartFile banner) {
        if (!eventCreateDTO.getType().equals("ART") && !eventCreateDTO.getType().equals("AUCTION")) {
            throw new BadCredentialsException();
        }

        EventEntity eventEntity = new EventEntity();
        eventEntity.setName(eventCreateDTO.getName());
        eventEntity.setDescription(eventCreateDTO.getDescription());
        eventEntity.setViews(0);
        eventEntity.setType(eventCreateDTO.getType());
        eventEntity.setStatus("WAIT");
        eventEntity.setStartDate(eventCreateDTO.getStartDate());
        eventEntity.setEndDate(eventCreateDTO.getEndDate());
        eventEntity.setPhotoUrl(fileService.saveFile(photo));
        eventEntity.setBannerUrl(fileService.saveFile(banner));

        eventRepository.save(eventEntity);
        notificationService.sendEventCreatedNotification(eventEntity);

        return eventEntity.getId();
    }

    public void changeEvent(EventChangeDTO eventChangeDTO, MultipartFile newPhoto, MultipartFile newBanner) {
        EventEntity eventEntity = eventRepository.findById(eventChangeDTO.getEventId()).orElseThrow(BadCredentialsException::new);

        if (!eventEntity.getStatus().equals("WAIT")) {
            throw new BadActionException("Event started");
        }

        eventEntity.setName(eventChangeDTO.getName());
        eventEntity.setDescription(eventChangeDTO.getDescription());
        eventEntity.setStartDate(eventChangeDTO.getStartDate());
        eventEntity.setEndDate(eventChangeDTO.getEndDate());

        if (eventChangeDTO.getChangeMainPhoto()) {
            if (!eventEntity.getPhotoUrl().isEmpty()) {
                fileService.deleteFile(eventEntity.getPhotoUrl());
            }
            eventEntity.setPhotoUrl(fileService.saveFile(newPhoto));
        }

        if (eventChangeDTO.getChangeBanner()) {
            if (!eventEntity.getBannerUrl().isEmpty()) {
                fileService.deleteFile(eventEntity.getBannerUrl());
            }
            eventEntity.setBannerUrl(fileService.saveFile(newBanner));
        }

        eventRepository.save(eventEntity);
    }

    public void deleteEvent(IntIdRequestDTO intIdRequestDTO) {
        EventEntity eventEntity = eventRepository.findById(intIdRequestDTO.getId()).orElseThrow(BadCredentialsException::new);

        if (!eventEntity.getStatus().equals("WAIT")) {
            throw new BadActionException("Event started");
        }

        if (eventEntity.getType().equals("ART")) {
            List<ArtEntity> arts = eventSubjectRepository.findAllByEventId(eventEntity.getId()).stream()
                    .map(eventSub -> artRepository.findById(eventSub.getId()).get())
                    .toList();
            for (ArtEntity artEntity: arts) {
                eventSubjectRepository.deleteBySubjectId(artEntity.getId());
                artPhotoRepository.findAllByArtId(artEntity.getId()).stream()
                        .map(ArtPhotoEntity::getPhotoUrl)
                        .forEach(fileService::deleteFile);

                artPhotoRepository.deleteAllByArtId(artEntity.getId());
                artRepository.deleteById(artEntity.getId());
            }
        } else {
            List<AuctionEntity> auctions = eventSubjectRepository.findAllByEventId(eventEntity.getId()).stream()
                    .map(eventSub -> auctionRepository.findById(eventSub.getId()).get())
                    .toList();
            for (AuctionEntity auctionEntity: auctions) {
                eventSubjectRepository.deleteBySubjectId(auctionEntity.getId());

                auctionPhotoRepository.findAllByAuctionId(auctionEntity.getId()).stream()
                        .map(AuctionPhotoEntity::getPhotoUrl)
                        .forEach(fileService::deleteFile);

                auctionPhotoRepository.deleteAllByAuctionId(auctionEntity.getId());
                auctionRepository.deleteById(auctionEntity.getId());
            }
        }

        notificationRepository.deleteAllBySubjectId(eventEntity.getId());
        eventRepository.deleteById(eventEntity.getId());
    }

    public Boolean checkAdmin(UUID id) {
        return adminRepository.existsById(id);
    }
}
