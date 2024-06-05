package ru.vsu.cs.sheina.online_gallery_backend.service;

import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.FluxSink;
import ru.vsu.cs.sheina.online_gallery_backend.configuration.AuctionSSE;
import ru.vsu.cs.sheina.online_gallery_backend.dto.auction.*;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.entity.*;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadActionException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentialsException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.ForbiddenActionException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.UserNotFoundException;
import ru.vsu.cs.sheina.online_gallery_backend.repository.*;
import ru.vsu.cs.sheina.online_gallery_backend.utils.JWTParser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final AuctionPhotoRepository auctionPhotoRepository;
    private final MaxRateRepository maxRateRepository;
    private final RateRepository rateRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final NotificationRepository notificationRepository;
    private final FileService fileService;
    private final NotificationService notificationService;
    private final CustomerRepository customerRepository;
    private final ArtistRepository artistRepository;
    private final EventRepository eventRepository;
    private final EventSubjectRepository eventSubjectRepository;
    private final JWTParser jwtParser;
    private final AdminService adminService;

    Map<AuctionSSE<UUID, Integer>, FluxSink<ServerSentEvent>> subscriptions = new HashMap<>();

    public Integer createAuction(AuctionCreateDTO auctionCreateDTO, List<MultipartFile> photos, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
        ArtistEntity artistEntity = artistRepository.findById(customerEntity.getArtistId()).orElseThrow(UserNotFoundException::new);

        if (!auctionCreateDTO.getType().equals("PHOTO") && !auctionCreateDTO.getType().equals("PAINTING") && !auctionCreateDTO.getType().equals("SCULPTURE")) {
            throw new BadCredentialsException();
        }

        AuctionEntity auctionEntity = new AuctionEntity();
        auctionEntity.setName(auctionCreateDTO.getName());
        auctionEntity.setType(auctionCreateDTO.getType());
        auctionEntity.setStartPrice(auctionCreateDTO.getStartPrice());
        auctionEntity.setCurrentPrice(auctionCreateDTO.getStartPrice());

        BigInteger rate = getRateFromPrice(auctionCreateDTO.getStartPrice());
        auctionEntity.setRate(rate);

        auctionEntity.setArtistId(artistEntity.getId());
        auctionEntity.setStatus("WAIT");
        auctionEntity.setDescription(auctionCreateDTO.getDescription());
        auctionEntity.setSize(auctionCreateDTO.getSize());
        auctionEntity.setCreateDate(auctionCreateDTO.getCreateDate());
        auctionEntity.setTags(auctionCreateDTO.getTags());
        auctionEntity.setMaterials(auctionCreateDTO.getMaterials());
        auctionEntity.setFrame(auctionCreateDTO.getFrame());
        auctionEntity.setPublishDate(new Timestamp(System.currentTimeMillis()));
        auctionEntity.setViews(0);

        if (auctionCreateDTO.getStartDate().compareTo(auctionCreateDTO.getEndDate()) >= 0) {
            throw new BadActionException("Incorrect date");
        }

        auctionEntity.setStartDate(auctionCreateDTO.getStartDate());
        auctionEntity.setEndDate(auctionCreateDTO.getEndDate());

        auctionRepository.save(auctionEntity);

        for(int i = 0; i < photos.size(); i++) {
            AuctionPhotoEntity auctionPhotoEntity = new AuctionPhotoEntity();
            auctionPhotoEntity.setAuctionId(auctionEntity.getId());
            auctionPhotoEntity.setDefaultPhoto(i == 0);
            auctionPhotoEntity.setPhotoUrl(fileService.saveFile(photos.get(i), auctionEntity.getId().toString()));

            auctionPhotoRepository.save(auctionPhotoEntity);
        }

        if (auctionCreateDTO.getEventId() != null) {
            EventEntity eventEntity = eventRepository.findById(auctionCreateDTO.getEventId()).orElseThrow(BadCredentialsException::new);
            if (!eventEntity.getStatus().equals("WAIT")) {
                throw new BadActionException("Event's not active");
            }

            if (!auctionEntity.getStartDate().before(eventEntity.getStartDate()) || !auctionEntity.getEndDate().after(eventEntity.getEndDate())) {
                throw new BadActionException("Bad auction timing");
            }

            EventSubjectEntity eventSubjectEntity = new EventSubjectEntity();
            eventSubjectEntity.setSubjectId(auctionEntity.getId());
            eventSubjectEntity.setEventId(eventEntity.getId());
            eventSubjectRepository.save(eventSubjectEntity);
        } else {
            notificationService.sendNewPublicAuctionNotification(artistEntity, auctionEntity);
        }

        return auctionEntity.getId();
    }

    public void changeAuction(AuctionChangeDTO auctionChangeDTO, List<MultipartFile> newPhotos, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
        ArtistEntity artistEntity = artistRepository.findById(customerEntity.getArtistId()).orElseThrow(UserNotFoundException::new);

        if (!auctionChangeDTO.getType().equals("PHOTO") && !auctionChangeDTO.getType().equals("PAINTING") && !auctionChangeDTO.getType().equals("SCULPTURE")) {
            throw new BadCredentialsException();
        }

        AuctionEntity auctionEntity = auctionRepository.findById(auctionChangeDTO.getAuctionId()).orElseThrow(BadCredentialsException::new);

        if (!artistEntity.getId().equals(auctionEntity.getArtistId())) {
            throw new ForbiddenActionException();
        }

        auctionEntity.setName(auctionChangeDTO.getName());
        auctionEntity.setType(auctionChangeDTO.getType());
        auctionEntity.setStartPrice(auctionChangeDTO.getStartPrice());
        auctionEntity.setCurrentPrice(auctionChangeDTO.getStartPrice());
        BigInteger rate = getRateFromPrice(auctionChangeDTO.getStartPrice());
        auctionEntity.setRate(rate);
        auctionEntity.setCreateDate(auctionChangeDTO.getCreateDate());
        auctionEntity.setDescription(auctionChangeDTO.getDescription());
        auctionEntity.setSize(auctionChangeDTO.getSize());
        auctionEntity.setFrame(auctionChangeDTO.getFrame());
        auctionEntity.setTags(auctionChangeDTO.getTags());
        auctionEntity.setMaterials(auctionChangeDTO.getMaterials());

        if (auctionChangeDTO.getStartDate().compareTo(auctionChangeDTO.getEndDate()) >= 0) {
            throw new BadActionException("Incorrect date");
        }

        auctionEntity.setStartDate(auctionChangeDTO.getStartDate());
        auctionEntity.setEndDate(auctionChangeDTO.getEndDate());

        for (String url: auctionChangeDTO.getDeletePhotoUrls()) {
            fileService.deleteFile(url);
            auctionPhotoRepository.deleteAllByAuctionIdAndPhotoUrl(auctionEntity.getId(), url);
        }

        for (int i = 0; i < newPhotos.size(); i++) {
            AuctionPhotoEntity auctionPhotoEntity = new AuctionPhotoEntity();
            auctionPhotoEntity.setAuctionId(auctionEntity.getId());
            auctionPhotoEntity.setPhotoUrl(fileService.saveFile(newPhotos.get(i), auctionEntity.getId().toString()));

            if (auctionChangeDTO.getChangeMainPhoto() && i == 0){
                Optional<AuctionPhotoEntity> mainPhoto = auctionPhotoRepository.findByAuctionIdAndDefaultPhoto(auctionEntity.getId(), true);
                if (mainPhoto.isPresent()) {
                    AuctionPhotoEntity mainPhotoEntity = mainPhoto.get();
                    mainPhotoEntity.setDefaultPhoto(false);
                    auctionPhotoRepository.save(mainPhotoEntity);
                }
                auctionPhotoEntity.setDefaultPhoto(true);
            } else {
                auctionPhotoEntity.setDefaultPhoto(false);
            }
            auctionPhotoRepository.save(auctionPhotoEntity);
        }
    }

    public void deleteAuction(IntIdRequestDTO intIdRequestDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
        UUID artistId = customerEntity.getArtistId();
        AuctionEntity auctionEntity = auctionRepository.findById(intIdRequestDTO.getId()).orElseThrow(BadCredentialsException::new);

        if (!artistId.equals(auctionEntity.getArtistId())) {
            throw new ForbiddenActionException();
        }

        notificationRepository.deleteAllBySubjectId(auctionEntity.getId());
        eventSubjectRepository.deleteAllBySubjectId(auctionEntity.getId());
        cartRepository.deleteAllBySubjectId(auctionEntity.getId());
        orderRepository.deleteAllBySubjectId(auctionEntity.getId());

        auctionPhotoRepository.findAllByAuctionId(auctionEntity.getId()).stream()
                .map(AuctionPhotoEntity::getPhotoUrl)
                .forEach(fileService::deleteFile);

        auctionPhotoRepository.deleteAllByAuctionId(auctionEntity.getId());
        auctionRepository.deleteById(auctionEntity.getId());
    }

    public AuctionFullDTO getAuction(Integer auctionId, String currentId) {
        AuctionEntity auctionEntity = auctionRepository.findById(auctionId).orElseThrow(BadCredentialsException::new);
        ArtistEntity artistEntity = artistRepository.findById(auctionEntity.getArtistId()).orElseThrow(UserNotFoundException::new);
        AuctionFullDTO dto = new AuctionFullDTO();

        if (eventSubjectRepository.existsBySubjectId(auctionId)) {
            EventSubjectEntity eventSubjectEntity = eventSubjectRepository.findBySubjectId(auctionId).get();
            EventEntity eventEntity = eventRepository.findById(eventSubjectEntity.getSubjectId()).orElseThrow(BadCredentialsException::new);
            if (eventEntity.getStatus().equals("WAIT") && currentId.equals("null")) {
                throw new ForbiddenActionException();
            } else if (eventEntity.getStatus().equals("WAIT")) {
                UUID userId = UUID.fromString(currentId);
                if (!auctionEntity.getArtistId().equals(userId) && !adminService.checkAdmin(userId)) {
                    throw new ForbiddenActionException();
                }
            }
            dto.setEventId(eventEntity.getId());
            dto.setEventName(eventEntity.getName());
        }

        dto.setAuctionId(auctionEntity.getId());
        dto.setName(auctionEntity.getName());
        dto.setType(auctionEntity.getType());
        dto.setLastPrice(auctionEntity.getCurrentPrice());
        dto.setArtistId(auctionEntity.getArtistId());
        dto.setArtistName(artistEntity.getArtistName());
        dto.setStatus(auctionEntity.getStatus());
        dto.setViewCount(auctionEntity.getViews());
        dto.setDescription(auctionEntity.getDescription());
        dto.setSize(auctionEntity.getSize());
        dto.setFrame(auctionEntity.getFrame());
        dto.setCreateDate(auctionEntity.getCreateDate());
        dto.setTags(auctionEntity.getTags());
        dto.setMaterials(auctionEntity.getMaterials());
        dto.setRate(auctionEntity.getRate());
        dto.setStartDate(auctionEntity.getStartDate());
        dto.setEndDate(auctionEntity.getEndDate());

        if (auctionEntity.getOwnerId() != null) {
            CustomerEntity customerEntity = customerRepository.findById(auctionEntity.getOwnerId()).orElseThrow(UserNotFoundException::new);
            dto.setCustomerId(auctionEntity.getOwnerId());
            dto.setCustomerName(customerEntity.getCustomerName());
            dto.setCustomerUrl(customerEntity.getAvatarUrl());
        } else {
            dto.setCustomerUrl(null);
            dto.setCustomerName(null);
            dto.setCustomerId(null);
        }

        List<AuctionPhotoEntity> auctionPhotoEntities = auctionPhotoRepository.findAllByAuctionId(auctionEntity.getId());

        List<String> urls = new ArrayList<>(auctionPhotoEntities.stream()
                .filter(AuctionPhotoEntity::getDefaultPhoto)
                .map(AuctionPhotoEntity::getPhotoUrl)
                .toList());

        for (AuctionPhotoEntity auctionPhotoEntity: auctionPhotoEntities) {
            if (!auctionPhotoEntity.getDefaultPhoto()) {
                urls.add(auctionPhotoEntity.getPhotoUrl());
            }
        }

        dto.setPhotoUrls(urls);

        int views = auctionEntity.getViews();
        auctionEntity.setViews(++views);
        artistRepository.save(artistEntity);

        if (!currentId.equals("null")) {
            CustomerEntity customerEntity = customerRepository.findById(UUID.fromString(currentId)).orElseThrow(UserNotFoundException::new);
            Optional<MaxRateEntity> maxRateOptional = maxRateRepository.findByAuctionIdAndCustomerId(auctionId, customerEntity.getId());
            maxRateOptional.ifPresent(maxRateEntity -> dto.setCurrentMaxRate(maxRateEntity.getRate()));

            List<RateEntity> rateEntities = rateRepository.findAllByAuctionId(auctionId);
            rateEntities.sort(Comparator.comparing(RateEntity::getCreateDate));

            List<RateDTO> rateDTOS = new ArrayList<>();

            for (RateEntity rateEntity : rateEntities) {
                RateDTO rateDTO = new RateDTO();
                rateDTO.setRate(rateEntity.getRate());

                if (rateEntity.getIsAnonymous()) {
                    rateDTO.setCustomerId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
                    rateDTO.setCustomerUrl("anonymous");
                    rateDTO.setCustomerName("anonymous");
                } else {
                    CustomerEntity customerRateEntity = customerRepository.findById(rateEntity.getCustomerId()).orElseThrow(UserNotFoundException::new);
                    rateDTO.setCustomerId(customerRateEntity.getId());
                    rateDTO.setCustomerUrl(customerRateEntity.getAvatarUrl());
                    rateDTO.setCustomerName(customerRateEntity.getCustomerName());
                }

                rateDTOS.add(rateDTO);
            }

            dto.setCustomerRates(rateDTOS);
        }

        return dto;
    }

    public List<AuctionShortDTO> getArtistAuctions(UUID artistId, String currentId) {
        ArtistEntity artistEntity = artistRepository.findById(artistId).orElseThrow(UserNotFoundException::new);

        List<AuctionEntity> auctionEntities = auctionRepository.findAllByArtistId(artistId);

        for (AuctionEntity auctionEntity: auctionEntities) {
            if (eventSubjectRepository.existsBySubjectId(auctionEntity.getId())) {
                EventSubjectEntity eventSubjectEntity = eventSubjectRepository.findBySubjectId(auctionEntity.getId()).get();
                EventEntity eventEntity = eventRepository.findById(eventSubjectEntity.getEventId()).orElseThrow(BadCredentialsException::new);
                if (eventEntity.getStatus().equals("WAIT") && currentId.equals("null")) {
                    auctionEntities.remove(auctionEntity);
                } else if (eventEntity.getStatus().equals("WAIT")) {
                    UUID userId = UUID.fromString(currentId);
                    if (!auctionEntity.getArtistId().equals(userId) && !adminService.checkAdmin(userId)) {
                        auctionEntities.remove(auctionEntity);
                    }
                }
            }
        }

        List<AuctionShortDTO> dtos = new ArrayList<>();

        for (AuctionEntity auctionEntity: auctionEntities) {
            AuctionShortDTO dto = new AuctionShortDTO();

            dto.setAuctionId(auctionEntity.getId());
            dto.setName(auctionEntity.getName());
            dto.setType(auctionEntity.getType());
            dto.setLastPrice(auctionEntity.getCurrentPrice());
            dto.setArtistId(artistId);
            dto.setArtistName(artistEntity.getArtistName());
            dto.setStatus(auctionEntity.getStatus());
            dto.setViewCount(auctionEntity.getViews());
            dto.setDescription(auctionEntity.getDescription());
            dto.setSize(auctionEntity.getSize());
            dto.setFrame(auctionEntity.getFrame());
            dto.setCreateDate(auctionEntity.getCreateDate());
            dto.setTags(auctionEntity.getTags());
            dto.setMaterials(auctionEntity.getMaterials());
            dto.setStartDate(auctionEntity.getStartDate());
            dto.setEndDate(auctionEntity.getEndDate());
            dto.setRateCount(rateRepository.countByAuctionId(auctionEntity.getId()));

            if (auctionEntity.getOwnerId() != null) {
                CustomerEntity customerEntity = customerRepository.findById(auctionEntity.getOwnerId()).orElseThrow(UserNotFoundException::new);
                dto.setCustomerId(auctionEntity.getOwnerId());
                dto.setCustomerName(customerEntity.getCustomerName());
                dto.setCustomerUrl(customerEntity.getAvatarUrl());
            } else {
                dto.setCustomerUrl(null);
                dto.setCustomerName(null);
                dto.setCustomerId(null);
            }

            Optional<AuctionPhotoEntity> auctionPhotoEntity = auctionPhotoRepository.findByAuctionIdAndDefaultPhoto(auctionEntity.getId(), true);
            if (auctionPhotoEntity.isPresent()) {
                dto.setPhotoUrl(auctionPhotoEntity.get().getPhotoUrl());
            } else {
                dto.setPhotoUrl("");
            }
            dtos.add(dto);
        }

        return dtos;
    }

    public List<AuctionShortDTO> searchAuctions(String input) {
        List<AuctionEntity> auctionEntities = auctionRepository.findAll().stream()
                .filter(ent -> ent.getName().toUpperCase().contains(input.toUpperCase()))
                .toList();

        for (AuctionEntity auctionEntity: auctionEntities) {
            if (eventSubjectRepository.existsBySubjectId(auctionEntity.getId())) {
                EventSubjectEntity eventSubjectEntity = eventSubjectRepository.findBySubjectId(auctionEntity.getId()).get();
                EventEntity eventEntity = eventRepository.findById(eventSubjectEntity.getEventId()).orElseThrow(BadCredentialsException::new);
                if (eventEntity.getStatus().equals("WAIT")) {
                    auctionEntities.remove(auctionEntity);
                }
            }
        }

        List<AuctionShortDTO> dtos = new ArrayList<>();

        for (AuctionEntity auctionEntity: auctionEntities) {
            AuctionShortDTO dto = new AuctionShortDTO();
            ArtistEntity artistEntity = artistRepository.findById(auctionEntity.getArtistId()).orElseThrow(UserNotFoundException::new);

            dto.setAuctionId(auctionEntity.getId());
            dto.setName(auctionEntity.getName());
            dto.setType(auctionEntity.getType());
            dto.setLastPrice(auctionEntity.getCurrentPrice());
            dto.setArtistId(auctionEntity.getArtistId());
            dto.setArtistName(artistEntity.getArtistName());
            dto.setStatus(auctionEntity.getStatus());
            dto.setViewCount(auctionEntity.getViews());
            dto.setDescription(auctionEntity.getDescription());
            dto.setSize(auctionEntity.getSize());
            dto.setFrame(auctionEntity.getFrame());
            dto.setCreateDate(auctionEntity.getCreateDate());
            dto.setTags(auctionEntity.getTags());
            dto.setMaterials(auctionEntity.getMaterials());
            dto.setStartDate(auctionEntity.getStartDate());
            dto.setEndDate(auctionEntity.getEndDate());
            dto.setRateCount(rateRepository.countByAuctionId(auctionEntity.getId()));

            if (auctionEntity.getOwnerId() != null) {
                CustomerEntity customerEntity = customerRepository.findById(auctionEntity.getOwnerId()).orElseThrow(UserNotFoundException::new);
                dto.setCustomerId(auctionEntity.getOwnerId());
                dto.setCustomerName(customerEntity.getCustomerName());
                dto.setCustomerUrl(customerEntity.getAvatarUrl());
            } else {
                dto.setCustomerUrl(null);
                dto.setCustomerName(null);
                dto.setCustomerId(null);
            }

            Optional<AuctionPhotoEntity> auctionPhotoEntity = auctionPhotoRepository.findByAuctionIdAndDefaultPhoto(auctionEntity.getId(), true);
            if (auctionPhotoEntity.isPresent()) {
                dto.setPhotoUrl(auctionPhotoEntity.get().getPhotoUrl());
            } else {
                dto.setPhotoUrl("");
            }
            dtos.add(dto);
        }

        return dtos;
    }

    public List<AuctionShortDTO> getAllAuctions() {
        List<AuctionEntity> auctionEntities = auctionRepository.findAll();

        for (AuctionEntity auctionEntity: auctionEntities) {
            if (eventSubjectRepository.existsBySubjectId(auctionEntity.getId())) {
                EventSubjectEntity eventSubjectEntity = eventSubjectRepository.findBySubjectId(auctionEntity.getId()).get();
                EventEntity eventEntity = eventRepository.findById(eventSubjectEntity.getEventId()).orElseThrow(BadCredentialsException::new);
                if (eventEntity.getStatus().equals("WAIT")) {
                    auctionEntities.remove(auctionEntity);
                }
            }
        }

        List<AuctionShortDTO> dtos = new ArrayList<>();

        for (AuctionEntity auctionEntity: auctionEntities) {
            AuctionShortDTO dto = new AuctionShortDTO();
            ArtistEntity artistEntity = artistRepository.findById(auctionEntity.getArtistId()).orElseThrow(UserNotFoundException::new);

            dto.setAuctionId(auctionEntity.getId());
            dto.setName(auctionEntity.getName());
            dto.setType(auctionEntity.getType());
            dto.setLastPrice(auctionEntity.getCurrentPrice());
            dto.setArtistId(auctionEntity.getArtistId());
            dto.setArtistName(artistEntity.getArtistName());
            dto.setStatus(auctionEntity.getStatus());
            dto.setViewCount(auctionEntity.getViews());
            dto.setDescription(auctionEntity.getDescription());
            dto.setSize(auctionEntity.getSize());
            dto.setFrame(auctionEntity.getFrame());
            dto.setCreateDate(auctionEntity.getCreateDate());
            dto.setTags(auctionEntity.getTags());
            dto.setMaterials(auctionEntity.getMaterials());
            dto.setStartDate(auctionEntity.getStartDate());
            dto.setEndDate(auctionEntity.getEndDate());
            dto.setRateCount(rateRepository.countByAuctionId(auctionEntity.getId()));

            if (auctionEntity.getOwnerId() != null) {
                CustomerEntity customerEntity = customerRepository.findById(auctionEntity.getOwnerId()).orElseThrow(UserNotFoundException::new);
                dto.setCustomerId(auctionEntity.getOwnerId());
                dto.setCustomerName(customerEntity.getCustomerName());
                dto.setCustomerUrl(customerEntity.getAvatarUrl());
            } else {
                dto.setCustomerUrl(null);
                dto.setCustomerName(null);
                dto.setCustomerId(null);
            }

            Optional<AuctionPhotoEntity> auctionPhotoEntity = auctionPhotoRepository.findByAuctionIdAndDefaultPhoto(auctionEntity.getId(), true);
            if (auctionPhotoEntity.isPresent()) {
                dto.setPhotoUrl(auctionPhotoEntity.get().getPhotoUrl());
            } else {
                dto.setPhotoUrl("");
            }
            dtos.add(dto);
        }

        return dtos;
    }

    public void createMaxRate(MaxRateCreateDTO maxRateCreateDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);

        AuctionEntity auctionEntity = auctionRepository.findById(maxRateCreateDTO.getAuctionId()).orElseThrow(BadCredentialsException::new);

        if (maxRateRepository.existsByAuctionIdAndAndCustomerId(maxRateCreateDTO.getAuctionId(), customerId)) {
            throw new BadActionException("You have already placed the max rate");
        }

        ArtistEntity artistEntity = artistRepository.findById(auctionEntity.getArtistId()).orElseThrow(UserNotFoundException::new);
        int comparison = maxRateCreateDTO.getMaxRate().compareTo(auctionEntity.getCurrentPrice());

        if (comparison <= 0) {
            throw new BadActionException("Rate is less than current price");
        }

        BigInteger newRate;

        Optional<MaxRateEntity> maxRateOptional = maxRateRepository.findByAuctionId(auctionEntity.getId());
        if (maxRateOptional.isPresent()) {
            MaxRateEntity secondMaxRateEntity = maxRateOptional.get();
            comparison = maxRateCreateDTO.getMaxRate().compareTo(secondMaxRateEntity.getRate());
            if (comparison < 0) {
                newRate = maxRateCreateDTO.getMaxRate().add(auctionEntity.getRate());
                createCustomerRate(auctionEntity.getId(), customerId, maxRateCreateDTO.getIsAnonymous(), maxRateCreateDTO.getMaxRate());
                createCustomerRate(auctionEntity.getId(), secondMaxRateEntity.getCustomerId(), secondMaxRateEntity.getIsAnonymous(), newRate);
                notificationService.sendMaxRateBlockNotification(auctionEntity, artistEntity, customerId);
            } else if (comparison == 0) {
                newRate = secondMaxRateEntity.getRate();
                createCustomerRate(auctionEntity.getId(), customerId, maxRateCreateDTO.getIsAnonymous(), maxRateCreateDTO.getMaxRate());
                createCustomerRate(auctionEntity.getId(), secondMaxRateEntity.getCustomerId(), secondMaxRateEntity.getIsAnonymous(), newRate);
                notificationService.sendMaxRateBlockNotification(auctionEntity, artistEntity, customerId);
            } else {
                newRate = secondMaxRateEntity.getRate().add(auctionEntity.getRate());
                createCustomerRate(auctionEntity.getId(), secondMaxRateEntity.getCustomerId(), secondMaxRateEntity.getIsAnonymous(), secondMaxRateEntity.getRate());
                createCustomerRate(auctionEntity.getId(), customerId, maxRateCreateDTO.getIsAnonymous(), newRate);
                notificationService.sendMaxRateBlockNotification(auctionEntity, artistEntity, secondMaxRateEntity.getCustomerId());

                maxRateRepository.deleteById(secondMaxRateEntity.getId());

                MaxRateEntity maxRateEntity = new MaxRateEntity();
                maxRateEntity.setRate(maxRateCreateDTO.getMaxRate());
                maxRateEntity.setAuctionId(auctionEntity.getId());
                maxRateEntity.setCustomerId(customerId);
                maxRateEntity.setIsAnonymous(maxRateCreateDTO.getIsAnonymous());
                maxRateEntity.setCreateDate(new Timestamp(System.currentTimeMillis()));

                maxRateRepository.save(maxRateEntity);
            }
            auctionEntity.setCurrentPrice(newRate);
            auctionRepository.save(auctionEntity);
        } else {
            newRate = auctionEntity.getCurrentPrice().add(auctionEntity.getRate());
            auctionEntity.setCurrentPrice(newRate);
            auctionRepository.save(auctionEntity);

            MaxRateEntity maxRateEntity = new MaxRateEntity();
            maxRateEntity.setRate(maxRateCreateDTO.getMaxRate());
            maxRateEntity.setAuctionId(auctionEntity.getId());
            maxRateEntity.setCustomerId(customerId);
            maxRateEntity.setIsAnonymous(maxRateCreateDTO.getIsAnonymous());
            maxRateEntity.setCreateDate(new Timestamp(System.currentTimeMillis()));

            maxRateRepository.save(maxRateEntity);

            createCustomerRate(auctionEntity.getId(), customerId, maxRateCreateDTO.getIsAnonymous(), newRate);
        }
    }

    public void createRate(RateCreateDTO rateCreateDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        AuctionEntity auctionEntity = auctionRepository.findById(rateCreateDTO.getAuctionId()).orElseThrow(BadCredentialsException::new);
        ArtistEntity artistEntity = artistRepository.findById(auctionEntity.getArtistId()).orElseThrow(UserNotFoundException::new);

        BigInteger rate = auctionEntity.getCurrentPrice().add(auctionEntity.getRate());
        Optional<MaxRateEntity> maxRateOptional = maxRateRepository.findByAuctionId(auctionEntity.getId());

        if (maxRateOptional.isPresent()) {
            MaxRateEntity maxRateEntity = maxRateOptional.get();
            int comparison = rate.compareTo(maxRateEntity.getRate());

            if (comparison == 0) {
                createCustomerRate(auctionEntity.getId(), maxRateEntity.getCustomerId(), maxRateEntity.getIsAnonymous(), rate);

                auctionEntity.setCurrentPrice(rate);
                auctionRepository.save(auctionEntity);
            } else if (comparison < 0) {
                BigInteger newRate = rate.add(auctionEntity.getRate());
                createCustomerRate(auctionEntity.getId(), customerId, rateCreateDTO.getIsAnonymous(), rate);
                createCustomerRate(auctionEntity.getId(), maxRateEntity.getCustomerId(), maxRateEntity.getIsAnonymous(), newRate);

                auctionEntity.setCurrentPrice(newRate);
                auctionRepository.save(auctionEntity);
            } else {
                createCustomerRate(auctionEntity.getId(), customerId, rateCreateDTO.getIsAnonymous(), rate);
                notificationService.sendMaxRateBlockNotification(auctionEntity, artistEntity, maxRateEntity.getCustomerId());

                maxRateRepository.deleteById(maxRateEntity.getAuctionId());

                auctionEntity.setCurrentPrice(rate);
                auctionRepository.save(auctionEntity);
            }
        } else {
            createCustomerRate(auctionEntity.getId(), customerId, rateCreateDTO.getIsAnonymous(), rate);
            auctionEntity.setCurrentPrice(rate);
            auctionRepository.save(auctionEntity);
        }
    }

    public void finishAuction(AuctionEntity auctionEntity) {
        if (rateRepository.existsByAuctionId(auctionEntity.getId())) {
            RateEntity rateEntity = rateRepository.findByRate(auctionEntity.getCurrentPrice()).get();
            CustomerEntity customerEntity = customerRepository.findById(rateEntity.getCustomerId()).orElseThrow(UserNotFoundException::new);
            ArtistEntity artistEntity = artistRepository.findById(auctionEntity.getArtistId()).orElseThrow(UserNotFoundException::new);

            auctionEntity.setStatus("SOLD");
            if (rateEntity.getIsAnonymous()) {
                auctionEntity.setOwnerId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
            } else {
                auctionEntity.setOwnerId(customerEntity.getId());
            }

            auctionRepository.save(auctionEntity);

            Integer orderId = orderService.createAuctionOrder(auctionEntity.getId(), auctionEntity.getArtistId(), customerEntity.getId());
            notificationService.sendAuctionWinningNotification(orderId, customerEntity, auctionEntity, artistEntity);
        } else {
            auctionPhotoRepository.deleteAllByAuctionId(auctionEntity.getId());
            auctionRepository.delete(auctionEntity);
        }
    }

    private void createCustomerRate(Integer auctionId, UUID customerId, Boolean isAnonymous, BigInteger rate) {
        RateEntity rateEntity = new RateEntity();
        rateEntity.setAuctionId(auctionId);
        rateEntity.setCustomerId(customerId);
        rateEntity.setIsAnonymous(isAnonymous);
        rateEntity.setRate(rate);
        rateEntity.setCreateDate(new Timestamp(System.currentTimeMillis()));

        rateRepository.save(rateEntity);

        for(AuctionSSE<UUID, Integer> sse: subscriptions.keySet()) {
            if (sse.getAuctionId().equals(auctionId)) {
                CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
                RateDTO rateDTO;
                if (isAnonymous) {
                    rateDTO = new RateDTO(UUID.fromString("00000000-0000-0000-0000-000000000000"), "anonymous", "anonymous", rate);
                } else {
                    rateDTO = new RateDTO(customerId, customerEntity.getCustomerName(), customerEntity.getAvatarUrl(), rate);
                }

                ServerSentEvent<Object> event = ServerSentEvent.builder()
                        .id(String.valueOf(rateEntity.getId()))
                        .event("AUCTION")
                        .data(rateDTO)
                        .build();
                subscriptions.get(sse).next(event);
            }
        }
    }

    public void deleteUserFromSubscriptions(UUID userId, Integer auctionId) {
        AuctionSSE<UUID, Integer> oldSubscription = new AuctionSSE<>(userId, auctionId);
        subscriptions.remove(oldSubscription);
    }

    public void addUserToSubscriptions(UUID userId, Integer auctionId, FluxSink<ServerSentEvent> fluxSink) {
        AuctionSSE<UUID, Integer> newSubscription = new AuctionSSE<>(userId, auctionId);
        subscriptions.put(newSubscription, fluxSink);
    }

    private BigInteger getRateFromPrice(BigInteger price) {
        BigDecimal decimalPrice = new BigDecimal(price);
        BigInteger rate = decimalPrice.divide(new BigDecimal(20), RoundingMode.CEILING).toBigInteger();
        return rate;
    }
}
