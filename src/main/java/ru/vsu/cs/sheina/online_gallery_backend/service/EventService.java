package ru.vsu.cs.sheina.online_gallery_backend.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.sheina.online_gallery_backend.dto.event.EventFullDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.event.EventShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.event.EventSubjectDTO;
import ru.vsu.cs.sheina.online_gallery_backend.entity.*;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentialsException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.ForbiddenActionException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.UserNotFoundException;
import ru.vsu.cs.sheina.online_gallery_backend.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ArtistRepository artistRepository;
    private final ArtRepository artRepository;
    private final NotificationService notificationService;
    private final AuctionRepository auctionRepository;
    private final ArtPhotoRepository artPhotoRepository;
    private final AuctionPhotoRepository auctionPhotoRepository;
    private final EventSubjectRepository eventSubjectRepository;
    private final AdminService adminService;

    public void startEvent(EventEntity eventEntity) {
        eventEntity.setStatus("AVAILABLE");

        if (eventEntity.getType().equals("ART")) {
            List<ArtEntity> artEntities = eventSubjectRepository.findAllByEventId(eventEntity.getId()).stream()
                    .map(event -> artRepository.findById(event.getSubjectId()).get())
                    .toList();
            for (ArtEntity artEntity: artEntities) {
                notificationService.sendNewPublicArtNotification(artEntity, artistRepository.findById(artEntity.getArtistId()).get());
            }
        }

        eventRepository.save(eventEntity);
    }

    public void finishEvent(EventEntity eventEntity) {
        eventEntity.setStatus("END");
        eventRepository.save(eventEntity);
    }

    public EventFullDTO getEvent(Integer eventId, String currentId) {
        EventEntity eventEntity = eventRepository.findById(eventId).orElseThrow(BadCredentialsException::new);

        EventFullDTO dto = new EventFullDTO();
        dto.setName(eventEntity.getName());
        dto.setPhotoUrl(eventEntity.getPhotoUrl());
        dto.setStartDate(eventEntity.getStartDate());
        dto.setEndDate(eventEntity.getEndDate());
        dto.setDescription(eventEntity.getDescription());
        dto.setType(eventEntity.getType());

        List<EventSubjectDTO> subjectDTOs = new ArrayList<>();

        if (!currentId.equals("null") && eventEntity.getStatus().equals("WAIT")) {
            UUID userId = UUID.fromString(currentId);
            if (adminService.checkAdmin(userId)) {
                List<EventSubjectEntity> subjectEntities = eventSubjectRepository.findAllByEventId(eventEntity.getId());
                subjectDTOs = getEventSubjects(eventEntity, subjectEntities);
            } else if (artistRepository.existsById(userId)) {
                List<EventSubjectEntity> subjectEntities = eventSubjectRepository.findAllByEventId(eventEntity.getId()).stream()
                        .filter(ent -> artRepository.findById(ent.getSubjectId()).get().getArtistId().equals(userId))
                        .toList();
                subjectDTOs = getEventSubjects(eventEntity, subjectEntities);
            }
        } else if (eventEntity.getStatus().equals("WAIT")) {
            subjectDTOs = null;
        } else {
            List<EventSubjectEntity> subjectEntities = eventSubjectRepository.findAllByEventId(eventEntity.getId());
            subjectDTOs = getEventSubjects(eventEntity, subjectEntities);
        }
        dto.setSubjects(subjectDTOs);
        return dto;
    }

    private List<EventSubjectDTO> getEventSubjects(EventEntity eventEntity, List<EventSubjectEntity> subjectEntities) {
        List<EventSubjectDTO> subjectDTOs = new ArrayList<>();

        for (EventSubjectEntity subjectEntity : subjectEntities) {
            if (eventEntity.getType().equals("ART")) {
                ArtEntity artEntity = artRepository.findById(subjectEntity.getSubjectId()).orElseThrow(BadCredentialsException::new);

                EventSubjectDTO subjectDTO = new EventSubjectDTO();
                subjectDTO.setSubjectId(artEntity.getId());
                subjectDTO.setArtistId(artEntity.getArtistId());
                subjectDTO.setSubjectName(artEntity.getName());

                if (artEntity.getSold()) {
                    subjectDTO.setStatus("SOLD");
                } else {
                    subjectDTO.setStatus("AVAILABLE");
                }

                subjectDTO.setPrice(artEntity.getPrice());
                subjectDTO.setViewCount(artEntity.getViews());
                subjectDTO.setSize(artEntity.getSize());
                subjectDTO.setCreateDate(artEntity.getCreateDate());
                subjectDTO.setTags(artEntity.getTags());
                subjectDTO.setMaterials(artEntity.getMaterials());
                subjectDTO.setFrame(artEntity.getFrame());

                ArtistEntity artistEntity = artistRepository.findById(artEntity.getArtistId()).orElseThrow(UserNotFoundException::new);
                subjectDTO.setArtistName(artistEntity.getArtistName());
                Optional<ArtPhotoEntity> artPhotoOpt = artPhotoRepository.findByArtIdAndAndDefaultPhoto(artEntity.getId(), true);

                if (artPhotoOpt.isPresent()) {
                    subjectDTO.setPhotoUrl(artPhotoOpt.get().getPhotoUrl());
                } else {
                    subjectDTO.setPhotoUrl("");
                }
                subjectDTOs.add(subjectDTO);
            } else {
                AuctionEntity auctionEntity = auctionRepository.findById(subjectEntity.getSubjectId()).orElseThrow(BadCredentialsException::new);

                EventSubjectDTO subjectDTO = new EventSubjectDTO();
                subjectDTO.setSubjectId(auctionEntity.getId());
                subjectDTO.setArtistId(auctionEntity.getArtistId());
                subjectDTO.setSubjectName(auctionEntity.getName());
                subjectDTO.setStatus(auctionEntity.getStatus());
                subjectDTO.setPrice(auctionEntity.getStartPrice());
                subjectDTO.setViewCount(auctionEntity.getViews());
                subjectDTO.setSize(auctionEntity.getSize());
                subjectDTO.setStartDate(auctionEntity.getStartDate());
                subjectDTO.setEndDate(auctionEntity.getEndDate());
                subjectDTO.setCreateDate(auctionEntity.getCreateDate());
                subjectDTO.setTags(auctionEntity.getTags());
                subjectDTO.setMaterials(auctionEntity.getMaterials());
                subjectDTO.setFrame(auctionEntity.getFrame());

                ArtistEntity artistEntity = artistRepository.findById(auctionEntity.getArtistId()).orElseThrow(UserNotFoundException::new);
                subjectDTO.setArtistName(artistEntity.getArtistName());
                Optional<AuctionPhotoEntity> auctionPhotoOpt = auctionPhotoRepository.findByAuctionIdAndDefaultPhoto(auctionEntity.getId(), true);

                if (auctionPhotoOpt.isPresent()) {
                    subjectDTO.setPhotoUrl(auctionPhotoOpt.get().getPhotoUrl());
                } else {
                    subjectDTO.setPhotoUrl("");
                }
                subjectDTOs.add(subjectDTO);
            }
        }
        return subjectDTOs;
    }

    public List<EventShortDTO> getEvents(String currentId) {
        List<EventEntity> eventEntities = eventRepository.findAll();

        if (currentId.equals("null")) {
            eventEntities = eventEntities.stream()
                    .filter(ent -> !ent.getStatus().equals("WAIT"))
                    .toList();
        } else {
            UUID userId = UUID.fromString(currentId);
            if (!adminService.checkAdmin(userId) && !artistRepository.existsById(userId)) {
                eventEntities = eventEntities.stream()
                        .filter(ent -> !ent.getStatus().equals("WAIT"))
                        .toList();
            }
        }

        List<EventShortDTO> dtos = new ArrayList<>();

        for (EventEntity eventEntity: eventEntities) {
            EventShortDTO eventShortDTO = new EventShortDTO();
            eventShortDTO.setEventId(eventEntity.getId());
            eventShortDTO.setPhotoUrl(eventEntity.getPhotoUrl());
            eventShortDTO.setName(eventEntity.getName());
            eventShortDTO.setStartDate(eventEntity.getStartDate());
            eventShortDTO.setEndDate(eventEntity.getEndDate());
            eventShortDTO.setDescription(eventEntity.getDescription());
            eventShortDTO.setStatus(eventEntity.getStatus());

            dtos.add(eventShortDTO);
        }
        return dtos;
    }
}
