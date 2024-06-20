package ru.vsu.cs.sheina.online_gallery_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.sheina.online_gallery_backend.dto.art.*;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.entity.*;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.*;
import ru.vsu.cs.sheina.online_gallery_backend.repository.*;
import ru.vsu.cs.sheina.online_gallery_backend.utils.JWTParser;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtService {

    private final JWTParser jwtParser;
    private final AdminService adminService;
    private final ArtRepository artRepository;
    private final ArtPhotoRepository artPhotoRepository;
    private final CustomerRepository customerRepository;
    private final ArtistRepository artistRepository;
    private final FileService fileService;
    private final CartRepository cartRepository;
    private final PrivateSubscriptionRepository privateSubscriptionRepository;
    private final ArtPrivateSubscriptionRepository artPrivateSubscriptionRepository;
    private final CustomerPrivateSubscriptionRepository customerPrivateSubscriptionRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final EventRepository eventRepository;
    private final EventSubjectRepository eventSubjectRepository;
    private final BlockUserRepository blockUserRepository;

    public void movePrivatePaintings(Integer subscriptionId) {
        artPrivateSubscriptionRepository.deleteAllBySubscriptionId(subscriptionId);
    }

    public Integer createArt(ArtCreateDTO artCreateDTO, List<MultipartFile> photos, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);

        if (blockUserRepository.existsById(customerId)) {
            throw new BlockUserException();
        }

        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
        UUID artistId = customerEntity.getArtistId();

        ArtistEntity artistEntity = artistRepository.findById(artistId).orElseThrow(UserNotFoundException::new);

        if (!artCreateDTO.getType().equals("PAINTING") && !artCreateDTO.getType().equals("PHOTO") && !artCreateDTO.getType().equals("SCULPTURE")) {
            throw new BadCredentialsException();
        }

        ArtEntity artEntity = new ArtEntity();
        artEntity.setName(artCreateDTO.getName());
        artEntity.setType(artCreateDTO.getType());
        artEntity.setPrice(artCreateDTO.getPrice());
        artEntity.setDescription(artCreateDTO.getDescription());
        artEntity.setSize(artCreateDTO.getSize());
        artEntity.setFrame(artCreateDTO.getFrame());
        artEntity.setTags(artCreateDTO.getTags());
        artEntity.setMaterials(artCreateDTO.getMaterials());
        artEntity.setOwnerId(null);
        artEntity.setSold(false);
        artEntity.setArtistId(artistId);
        artEntity.setPublishDate(new Timestamp(System.currentTimeMillis()));
        artEntity.setCreateDate(artCreateDTO.getCreateDate());
        artEntity.setViews(0);

        artRepository.save(artEntity);

        boolean privateArt = artCreateDTO.getIsPrivate();

        if (privateArt && !privateSubscriptionRepository.existsByArtistId(artistId)) {
            throw new BadActionException("You don't have a private subscription");
        } else if (privateArt){
            PrivateSubscriptionEntity subscription = privateSubscriptionRepository.findByArtistId(artistId).get();
            ArtPrivateSubscriptionEntity artPrivateSubscription = new ArtPrivateSubscriptionEntity();
            artPrivateSubscription.setArtId(artEntity.getId());
            artPrivateSubscription.setSubscriptionId(subscription.getId());
            artPrivateSubscriptionRepository.save(artPrivateSubscription);
        }

        for(int i = 0; i < photos.size(); i++) {
            ArtPhotoEntity artPhotoEntity = new ArtPhotoEntity();
            artPhotoEntity.setArtId(artEntity.getId());
            artPhotoEntity.setPhotoUrl(fileService.saveFile(photos.get(i), artEntity.getId().toString()));
            artPhotoEntity.setDefaultPhoto(i == 0);
            artPhotoRepository.save(artPhotoEntity);
        }

        if (artCreateDTO.getEventId() != null) {
            EventEntity eventEntity = eventRepository.findById(artCreateDTO.getEventId()).orElseThrow(BadCredentialsException::new);
            if (!eventEntity.getStatus().equals("WAIT")) {
                throw new BadActionException("Event's not active");
            }
            EventSubjectEntity eventSubjectEntity = new EventSubjectEntity();
            eventSubjectEntity.setSubjectId(artEntity.getId());
            eventSubjectEntity.setEventId(eventEntity.getId());
            eventSubjectRepository.save(eventSubjectEntity);
        }

        if (artCreateDTO.getIsPrivate()) {
            PrivateSubscriptionEntity privateSubscription = privateSubscriptionRepository.findByArtistId(artistId).get();
            notificationService.sendNewPrivateArtNotification(artEntity, artistEntity, privateSubscription);
        } else if (artCreateDTO.getEventId() == null){
            notificationService.sendNewPublicArtNotification(artEntity, artistEntity);
        }

        artRepository.save(artEntity);

        return artEntity.getId();
    }

    public ArtFullDTO getArt(Integer artId, String currentId) {
        ArtEntity artEntity = artRepository.findById(artId).orElseThrow(BadCredentialsException::new);
        ArtistEntity artistEntity = artistRepository.findById(artEntity.getArtistId()).orElseThrow(UserNotFoundException::new);
        ArtFullDTO artFullDTO = new ArtFullDTO();

        if (blockUserRepository.existsById(artistEntity.getId())) {
            throw new BlockUserException();
        }

        if (eventSubjectRepository.existsBySubjectId(artId)) {
            EventSubjectEntity eventSubjectEntity = eventSubjectRepository.findBySubjectId(artId).get();
            EventEntity eventEntity = eventRepository.findById(eventSubjectEntity.getEventId()).orElseThrow(BadCredentialsException::new);
            if (eventEntity.getStatus().equals("WAIT") && currentId.equals("null")) {
                throw new ForbiddenActionException();
            } else if (eventEntity.getStatus().equals("WAIT")) {
                UUID userId = UUID.fromString(currentId);
                if (!artEntity.getArtistId().equals(userId) && !adminService.checkAdmin(userId)) {
                    throw new ForbiddenActionException();
                }
            }
            artFullDTO.setEventId(eventEntity.getId());
            artFullDTO.setEventName(eventEntity.getName());
        }

        artFullDTO.setName(artEntity.getName());
        artFullDTO.setType(artEntity.getType());
        artFullDTO.setPrice(artEntity.getPrice());
        artFullDTO.setArtistId(artEntity.getArtistId());
        artFullDTO.setArtistName(artistEntity.getArtistName());
        artFullDTO.setDescription(artEntity.getDescription());
        artFullDTO.setSize(artEntity.getSize());
        artFullDTO.setCreateDate(artEntity.getCreateDate());
        artFullDTO.setTags(artEntity.getTags());
        artFullDTO.setMaterials(artEntity.getMaterials());
        artFullDTO.setFrame(artEntity.getFrame());
        artFullDTO.setPublishDate(artEntity.getPublishDate());

        if (currentId.equals("null") && artPrivateSubscriptionRepository.existsByArtId(artId)) {
            throw new ForbiddenActionException();
        } else if (!currentId.equals("null") && !artPrivateSubscriptionRepository.existsByArtId(artId)) {
            artFullDTO.setStatus("AVAILABLE");
            artFullDTO.setIsPrivate(false);
        } else if (!currentId.equals("null") && artPrivateSubscriptionRepository.existsByArtId(artId)){
            UUID userId = UUID.fromString(currentId);
            PrivateSubscriptionEntity privateSubscriptionEntity = privateSubscriptionRepository.findByArtistId(artEntity.getArtistId()).get();
            Optional<CustomerEntity> currentCustomerOpt = customerRepository.findById(userId);
            Optional<ArtistEntity> currentArtistOpt = artistRepository.findById(userId);

            if (currentCustomerOpt.isPresent()) {
                if (!customerPrivateSubscriptionRepository.existsByCustomerIdAndPrivateSubscriptionId(userId, privateSubscriptionEntity.getId()) &&
                        !currentCustomerOpt.get().getArtistId().equals(artEntity.getArtistId()) && !adminService.checkAdmin(userId)) {
                    throw new ForbiddenActionException();
                }
            } else {
                CustomerEntity customerEntity = customerRepository.findByArtistId(currentArtistOpt.get().getId()).get();
                if (!customerPrivateSubscriptionRepository.existsByCustomerIdAndPrivateSubscriptionId(customerEntity.getId(), privateSubscriptionEntity.getId()) &&
                        !userId.equals(artEntity.getArtistId()) && !adminService.checkAdmin(userId)) {
                    throw new ForbiddenActionException();
                }
            }
            artFullDTO.setIsPrivate(true);
        } else {
            artFullDTO.setIsPrivate(false);
        }

        if (artEntity.getOwnerId() != null) {
            if (!blockUserRepository.existsById(artEntity.getOwnerId())) {
                CustomerEntity customerEntity = customerRepository.findById(artEntity.getOwnerId()).orElseThrow(UserNotFoundException::new);
                artFullDTO.setCustomerId(artEntity.getOwnerId());
                artFullDTO.setCustomerName(customerEntity.getCustomerName());
                artFullDTO.setStatus("SOLD");
            }
        }

        if (currentId.equals("null") && artEntity.getOwnerId() == null) {
            artFullDTO.setStatus("AVAILABLE");
        } else if (!currentId.equals("null") && artEntity.getOwnerId() == null) {
            UUID customerId = UUID.fromString(currentId);
            if (cartRepository.existsByCustomerIdAndSubjectId(customerId, artId)) {
                artFullDTO.setStatus("CART");
            } else {
                artFullDTO.setStatus("AVAILABLE");
            }
        }

        List<ArtPhotoEntity> artPhotoEntities = artPhotoRepository.findAllByArtId(artEntity.getId());

        List<String> urls = new ArrayList<>(artPhotoEntities.stream()
                .filter(ArtPhotoEntity::getDefaultPhoto)
                .map(ArtPhotoEntity::getPhotoUrl)
                .toList());

        for (ArtPhotoEntity artPhotoEntity: artPhotoEntities) {
            if (!artPhotoEntity.getDefaultPhoto()) {
                urls.add(artPhotoEntity.getPhotoUrl());
            }
        }

        artFullDTO.setPhotoUrls(urls);

        int views = artEntity.getViews();
        artEntity.setViews(++views);
        artRepository.save(artEntity);

        return artFullDTO;
    }

    public void changeArt(ArtChangeDTO artChangeDTO, List<MultipartFile> newPhotos, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);

        if (blockUserRepository.existsById(customerId)) {
            throw new BlockUserException();
        }

        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
        UUID artistId = customerEntity.getArtistId();

        if (!artistRepository.existsById(artistId)) {
            throw new UserNotFoundException();
        }

        if (!artChangeDTO.getType().equals("PAINTING") && !artChangeDTO.getType().equals("PHOTO") && !artChangeDTO.getType().equals("SCULPTURE")) {
            throw new BadCredentialsException();
        }

        ArtEntity artEntity = artRepository.findById(artChangeDTO.getArtId()).orElseThrow(BadCredentialsException::new);

        if (!artistId.equals(artEntity.getArtistId())) {
            throw new ForbiddenActionException();
        }

        artEntity.setName(artChangeDTO.getName());
        artEntity.setCreateDate(artChangeDTO.getCreateDate());
        artEntity.setType(artChangeDTO.getType());
        artEntity.setPrice(artChangeDTO.getPrice());
        artEntity.setDescription(artChangeDTO.getDescription());
        artEntity.setSize(artChangeDTO.getSize());
        artEntity.setFrame(artChangeDTO.getFrame());
        artEntity.setTags(artChangeDTO.getTags());
        artEntity.setMaterials(artChangeDTO.getMaterials());

        for (String url: artChangeDTO.getDeletePhotoUrls()) {
            fileService.deleteFile(url);
            artPhotoRepository.deleteAllByArtIdAndAndPhotoUrl(artEntity.getId(), url);
        }

        for (int i = 0; i < newPhotos.size(); i++) {
            if (!newPhotos.get(i).isEmpty()) {
                ArtPhotoEntity artPhotoEntity = new ArtPhotoEntity();
                artPhotoEntity.setArtId(artEntity.getId());
                artPhotoEntity.setPhotoUrl(fileService.saveFile(newPhotos.get(i), artEntity.getId().toString()));

                if (artChangeDTO.getChangeMainPhoto() && i == 0) {
                    Optional<ArtPhotoEntity> mainPhoto = artPhotoRepository.findByArtIdAndAndDefaultPhoto(artEntity.getId(), true);
                    if (mainPhoto.isPresent()) {
                        ArtPhotoEntity mainPhotoEntity = mainPhoto.get();
                        mainPhotoEntity.setDefaultPhoto(false);
                        artPhotoRepository.save(mainPhotoEntity);
                    }
                    artPhotoEntity.setDefaultPhoto(true);
                } else {
                    artPhotoEntity.setDefaultPhoto(false);
                }
                artPhotoRepository.save(artPhotoEntity);
            }
        }

        if (artChangeDTO.getIsPrivate() && !privateSubscriptionRepository.existsByArtistId(artistId)) {
            throw new BadActionException("You don't have a private subscription");
        } else if (artChangeDTO.getIsPrivate() && !artPrivateSubscriptionRepository.existsByArtId(artEntity.getId())){
            PrivateSubscriptionEntity subscription = privateSubscriptionRepository.findByArtistId(artistId).get();
            ArtPrivateSubscriptionEntity artPrivateSubscription = new ArtPrivateSubscriptionEntity();
            artPrivateSubscription.setArtId(artEntity.getId());
            artPrivateSubscription.setSubscriptionId(subscription.getId());
            artPrivateSubscriptionRepository.save(artPrivateSubscription);
        } else if (!artChangeDTO.getIsPrivate() && artPrivateSubscriptionRepository.existsByArtId(artEntity.getId())) {
            artPrivateSubscriptionRepository.deleteAllByArtId(artEntity.getId());
        }

        artRepository.save(artEntity);
    }

    public void deleteArt(IntIdRequestDTO intIdRequestDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);

        if (blockUserRepository.existsById(customerId)) {
            throw new BlockUserException();
        }

        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
        UUID artistId = customerEntity.getArtistId();
        ArtEntity artEntity = artRepository.findById(intIdRequestDTO.getId()).orElseThrow(BadCredentialsException::new);

        if (!artistId.equals(artEntity.getArtistId())) {
            throw new ForbiddenActionException();
        }

        notificationRepository.deleteAllBySubjectId(artEntity.getId());
        eventSubjectRepository.deleteAllBySubjectId(artEntity.getId());
        cartRepository.deleteAllBySubjectId(artEntity.getId());
        orderRepository.deleteAllBySubjectId(artEntity.getId());
        artPrivateSubscriptionRepository.deleteAllByArtId(artEntity.getId());

        artPhotoRepository.findAllByArtId(artEntity.getId()).stream()
                .map(ArtPhotoEntity::getPhotoUrl)
                .forEach(fileService::deleteFile);

        artPhotoRepository.deleteAllByArtId(artEntity.getId());
        artRepository.deleteById(artEntity.getId());
    }

    public List<ArtistArtDTO> getArtistArt(UUID artistId, String currentId) {
        ArtistEntity artistEntity = artistRepository.findById(artistId).orElseThrow(UserNotFoundException::new);

        if (currentId.equals("null") && blockUserRepository.existsById(artistId)) {
            throw new BlockUserException();
        } else if (!currentId.equals("null") && blockUserRepository.existsById(artistId)) {
            if (!adminService.checkAdmin(UUID.fromString(currentId))) {
                throw new BlockUserException();
            }
        }

        List<ArtEntity> artEntities = artRepository.findAllByArtistId(artistId);

        for (ArtEntity artEntity: artEntities) {
            if (eventSubjectRepository.existsBySubjectId(artEntity.getId())) {
                EventSubjectEntity eventSubjectEntity = eventSubjectRepository.findBySubjectId(artEntity.getId()).get();
                EventEntity eventEntity = eventRepository.findById(eventSubjectEntity.getEventId()).orElseThrow(BadCredentialsException::new);
                if (eventEntity.getStatus().equals("WAIT") && currentId.equals("null")) {
                    artEntities.remove(artEntity);
                } else if (eventEntity.getStatus().equals("WAIT")) {
                    UUID userId = UUID.fromString(currentId);
                    if (!artEntity.getArtistId().equals(userId) && !adminService.checkAdmin(userId)) {
                        artEntities.remove(artEntity);
                    }
                }
            }
        }

        List<ArtistArtDTO> dtos = new ArrayList<>();

        for (ArtEntity artEntity: artEntities) {
            ArtistArtDTO dto = new ArtistArtDTO();
            dto.setArtId(artEntity.getId());
            dto.setArtistName(artistEntity.getArtistName());
            dto.setName(artEntity.getName());
            dto.setPrice(artEntity.getPrice());

            Optional<ArtPhotoEntity> artPhotoEntity = artPhotoRepository.findByArtIdAndAndDefaultPhoto(artEntity.getId(), true);
            if (artPhotoEntity.isPresent()) {
                dto.setPhotoUrl(artPhotoEntity.get().getPhotoUrl());
            } else {
                dto.setPhotoUrl("");
            }

            if (artEntity.getOwnerId() != null) {
                CustomerEntity customerEntity = customerRepository.findById(artEntity.getOwnerId()).orElseThrow(UserNotFoundException::new);
                dto.setCustomerId(customerEntity.getId());
                dto.setCustomerName(customerEntity.getCustomerName());
                dto.setAvatarUrl(customerEntity.getAvatarUrl());
            } else {
                dto.setCustomerId(null);
                dto.setCustomerName(null);
                dto.setAvatarUrl(null);
            }

            Optional<ArtPrivateSubscriptionEntity> artPrivSub = artPrivateSubscriptionRepository.findByArtId(artEntity.getId());
            if (artPrivSub.isPresent()) {
                dto.setIsPrivate(true);
                if (!currentId.equals("null")) {
                    UUID customerId = UUID.fromString(currentId);
                    CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
                    if (customerEntity.getArtistId() != null) {
                        dto.setAvailable(customerPrivateSubscriptionRepository.existsByCustomerIdAndPrivateSubscriptionId(customerId, artPrivSub.get().getSubscriptionId())|| adminService.checkAdmin(customerId)
                        || customerEntity.getArtistId().equals(artEntity.getArtistId()));
                    } else {
                        dto.setAvailable(customerPrivateSubscriptionRepository.existsByCustomerIdAndPrivateSubscriptionId(customerId, artPrivSub.get().getSubscriptionId())|| adminService.checkAdmin(customerId));
                    }
                    dto.setAvailable(customerPrivateSubscriptionRepository.existsByCustomerIdAndPrivateSubscriptionId(customerId, artPrivSub.get().getSubscriptionId())|| adminService.checkAdmin(customerId));
                } else {
                    dto.setAvailable(false);
                }
            } else {
                dto.setAvailable(true);
                dto.setIsPrivate(false);
            }
            dtos.add(dto);
        }
        return dtos;
    }

    public List<CustomerArtDTO> getCustomerArt(UUID customerId) {
        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);

        if (blockUserRepository.existsById(customerId)) {
            throw new BlockUserException();
        }

        List<ArtEntity> artEntities = artRepository.findAllByOwnerId(customerId);
        List<CustomerArtDTO> dtos = new ArrayList<>();

        for (ArtEntity artEntity: artEntities) {
            CustomerArtDTO dto = new CustomerArtDTO();
            dto.setArtId(artEntity.getId());
            dto.setName(artEntity.getName());
            dto.setPrice(artEntity.getPrice());
            dto.setCustomerName(customerEntity.getCustomerName());

            ArtistEntity artistEntity = artistRepository.findById(artEntity.getArtistId()).orElseThrow(UserNotFoundException::new);
            dto.setArtistName(artistEntity.getArtistName());

            dto.setIsPrivate(artPrivateSubscriptionRepository.existsByArtId(artEntity.getId()));

            Optional<ArtPhotoEntity> artPhotoEntity = artPhotoRepository.findByArtIdAndAndDefaultPhoto(artEntity.getId(), true);
            if (artPhotoEntity.isPresent()) {
                dto.setPhotoUrl(artPhotoEntity.get().getPhotoUrl());
            } else {
                dto.setPhotoUrl("");
            }

            dtos.add(dto);
        }
        return dtos;
    }

    public List<CommonArtDTO> getAllArtsByType(String type) {
        if (!type.equals("paintings") && !type.equals("photos") && !type.equals("sculptures")) {
            throw new BadCredentialsException();
        }
        String artType = switch (type) {
            case "paintings" -> "PAINTING";
            case "photos" -> "PHOTO";
            case "sculptures" -> "SCULPTURE";
            default -> "";
        };

        List<ArtEntity> entities = new ArrayList<>();
        artRepository.findAllByType(artType).stream()
                .filter(art -> !artPrivateSubscriptionRepository.existsByArtId(art.getId()) || art.getSold())
                .filter(art -> !blockUserRepository.existsById(art.getArtistId()))
                .forEach(entities::add);

        for (Iterator<ArtEntity> iterator = entities.iterator(); iterator.hasNext(); ) {
            ArtEntity artEntity = iterator.next();
            if (eventSubjectRepository.existsBySubjectId(artEntity.getId())) {
                EventSubjectEntity eventSubjectEntity = eventSubjectRepository.findBySubjectId(artEntity.getId()).get();
                EventEntity eventEntity = eventRepository.findById(eventSubjectEntity.getEventId()).orElseThrow(BadCredentialsException::new);
                if (eventEntity.getStatus().equals("WAIT")) {
                    iterator.remove();
                }
            }
        }

        List<CommonArtDTO> dtos = new ArrayList<>();

        for (ArtEntity entity: entities) {
            dtos.add(commonArtDTOByEntity(entity));
        }

        return dtos;
    }

    public List<CommonArtDTO> searchPaintings(String input) {
        List<ArtEntity> artEntities = new ArrayList<>();
        artRepository.findAll().stream()
                .filter(art -> !artPrivateSubscriptionRepository.existsByArtId(art.getId()) || art.getSold())
                .filter(art -> art.getType().equals("PAINTING"))
                .filter(art -> !blockUserRepository.existsById(art.getArtistId()))
                .filter(art -> art.getName().toUpperCase().contains(input.toUpperCase()))
                .forEach(artEntities::add);

        for (Iterator<ArtEntity> iterator = artEntities.iterator(); iterator.hasNext(); ) {
            ArtEntity artEntity = iterator.next();
            if (eventSubjectRepository.existsBySubjectId(artEntity.getId())) {
                EventSubjectEntity eventSubjectEntity = eventSubjectRepository.findBySubjectId(artEntity.getId()).get();
                EventEntity eventEntity = eventRepository.findById(eventSubjectEntity.getEventId()).orElseThrow(BadCredentialsException::new);
                if (eventEntity.getStatus().equals("WAIT")) {
                    iterator.remove();
                }
            }
        }

        List<CommonArtDTO> dtos = new ArrayList<>();

        for (ArtEntity art: artEntities) {
            dtos.add(commonArtDTOByEntity(art));
        }
        return dtos;
    }

    public List<CommonArtDTO> searchPhotos(String input) {
        List<ArtEntity> artEntities = new ArrayList<>();
        artRepository.findAll().stream()
                .filter(art -> !artPrivateSubscriptionRepository.existsByArtId(art.getId()) || art.getSold())
                .filter(art -> art.getType().equals("PHOTO"))
                .filter(art -> !blockUserRepository.existsById(art.getArtistId()))
                .filter(art -> art.getName().toUpperCase().contains(input.toUpperCase()))
                .forEach(artEntities::add);

        for (Iterator<ArtEntity> iterator = artEntities.iterator(); iterator.hasNext(); ) {
            ArtEntity artEntity = iterator.next();
            if (eventSubjectRepository.existsBySubjectId(artEntity.getId())) {
                EventSubjectEntity eventSubjectEntity = eventSubjectRepository.findBySubjectId(artEntity.getId()).get();
                EventEntity eventEntity = eventRepository.findById(eventSubjectEntity.getEventId()).orElseThrow(BadCredentialsException::new);
                if (eventEntity.getStatus().equals("WAIT")) {
                    iterator.remove();
                }
            }
        }

        List<CommonArtDTO> dtos = new ArrayList<>();

        for (ArtEntity art: artEntities) {
            dtos.add(commonArtDTOByEntity(art));
        }
        return dtos;
    }

    public List<CommonArtDTO> searchSculptures(String input) {
        List<ArtEntity> artEntities = new ArrayList<>();
        artRepository.findAll().stream()
                .filter(art -> !artPrivateSubscriptionRepository.existsByArtId(art.getId()) || art.getSold())
                .filter(art -> art.getType().equals("SCULPTURE"))
                .filter(art -> !blockUserRepository.existsById(art.getArtistId()))
                .filter(art -> art.getName().toUpperCase().contains(input.toUpperCase()))
                .forEach(artEntities::add);

        for (Iterator<ArtEntity> iterator = artEntities.iterator(); iterator.hasNext(); ) {
            ArtEntity artEntity = iterator.next();
            if (eventSubjectRepository.existsBySubjectId(artEntity.getId())) {
                EventSubjectEntity eventSubjectEntity = eventSubjectRepository.findBySubjectId(artEntity.getId()).get();
                EventEntity eventEntity = eventRepository.findById(eventSubjectEntity.getEventId()).orElseThrow(BadCredentialsException::new);
                if (eventEntity.getStatus().equals("WAIT")) {
                    iterator.remove();
                }
            }
        }

        List<CommonArtDTO> dtos = new ArrayList<>();

        for (ArtEntity art: artEntities) {
            dtos.add(commonArtDTOByEntity(art));
        }
        return dtos;
    }

    private CommonArtDTO commonArtDTOByEntity(ArtEntity entity) {
        CommonArtDTO dto = new CommonArtDTO();
        dto.setArtId(entity.getId());
        dto.setName(entity.getName());
        dto.setPrice(entity.getPrice());
        dto.setSize(entity.getSize());
        dto.setCreateDate(entity.getCreateDate());
        dto.setTags(entity.getTags());
        dto.setMaterials(entity.getMaterials());
        dto.setFrame(entity.getFrame());
        dto.setViewCount(entity.getViews());

        ArtistEntity artistEntity = artistRepository.findById(entity.getArtistId()).get();

        dto.setArtistId(artistEntity.getId());
        dto.setArtistName(artistEntity.getArtistName());

        if(artPrivateSubscriptionRepository.existsByArtId(entity.getId())) {
            dto.setIsPrivate(true);
        } else {
            dto.setIsPrivate(false);
        }

        if (entity.getOwnerId() != null) {
            if (!blockUserRepository.existsById(entity.getArtistId()) && customerRepository.existsById(entity.getOwnerId())) {
                CustomerEntity customerEntity = customerRepository.findById(entity.getOwnerId()).get();
                dto.setCustomerId(customerEntity.getId());
                dto.setCustomerName(customerEntity.getCustomerName());
                dto.setAvatarUrl(customerEntity.getAvatarUrl());
            }
        }

        Optional<ArtPhotoEntity> artPhotoEntity = artPhotoRepository.findByArtIdAndAndDefaultPhoto(entity.getId(), true);
        if (artPhotoEntity.isPresent()) {
            dto.setPhotoUrl(artPhotoEntity.get().getPhotoUrl());
        } else {
            dto.setPhotoUrl("");
        }
        return dto;
    }
}
