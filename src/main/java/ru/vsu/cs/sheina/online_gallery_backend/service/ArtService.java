package ru.vsu.cs.sheina.online_gallery_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.sheina.online_gallery_backend.dto.art.*;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.entity.*;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadActionException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentialsException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.ForbiddenActionException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.UserNotFoundException;
import ru.vsu.cs.sheina.online_gallery_backend.repository.*;
import ru.vsu.cs.sheina.online_gallery_backend.utils.JWTParser;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArtService {

    private final JWTParser jwtParser;
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

    public void movePrivatePaintings(Integer subscriptionId) {
        artPrivateSubscriptionRepository.deleteAllBySubscriptionId(subscriptionId);
    }

    public void createArt(ArtCreateDTO artCreateDTO, List<MultipartFile> photos, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
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
            artPhotoEntity.setPhotoUrl(fileService.saveFile(photos.get(i)));
            artPhotoEntity.setDefaultPhoto(i == 0);
            artPhotoRepository.save(artPhotoEntity);
        }

        if (artCreateDTO.getIsPrivate()) {
            PrivateSubscriptionEntity privateSubscription = privateSubscriptionRepository.findByArtistId(artistId).get();
            notificationService.sendNewPrivateArtNotification(artEntity, artistEntity, privateSubscription);
        } else {
            notificationService.sendNewPublicArtNotification(artEntity, artistEntity);
        }
    }

    public ArtFullDTO getArt(Integer artId, String currentId) {
        ArtEntity artEntity = artRepository.findById(artId).orElseThrow(BadCredentialsException::new);
        ArtistEntity artistEntity = artistRepository.findById(artEntity.getArtistId()).orElseThrow(UserNotFoundException::new);

        ArtFullDTO artFullDTO = new ArtFullDTO();
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
            UUID customerId = UUID.fromString(currentId);
            CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
            PrivateSubscriptionEntity privateSubscriptionEntity = privateSubscriptionRepository.findByArtistId(artEntity.getArtistId()).get();
            if (!customerPrivateSubscriptionRepository.existsByCustomerIdAndPrivateSubscriptionId(customerId, privateSubscriptionEntity.getId()) &&
                    !customerEntity.getArtistId().equals(artEntity.getArtistId())) {
                throw new ForbiddenActionException();
            }
            artFullDTO.setIsPrivate(true);
        } else {
            artFullDTO.setIsPrivate(false);
        }

        if (artEntity.getOwnerId() != null) {
            CustomerEntity customerEntity = customerRepository.findById(artEntity.getOwnerId()).orElseThrow(UserNotFoundException::new);
            artFullDTO.setCustomerId(artEntity.getOwnerId());
            artFullDTO.setCustomerName(customerEntity.getCustomerName());
            artFullDTO.setStatus("SOLD");
        } else {
            artFullDTO.setCustomerName(null);
            artFullDTO.setCustomerId(null);
        }

        if (currentId.equals("null") && artEntity.getOwnerId() == null) {
            artFullDTO.setStatus("AVAILABLE");
        } else if (!currentId.equals("null") && artEntity.getOwnerId() == null) {
            UUID customerId = UUID.fromString(currentId);
            if (cartRepository.existsByCustomerIdAndArtId(customerId, artId)) {
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
            ArtPhotoEntity artPhotoEntity = new ArtPhotoEntity();
            artPhotoEntity.setArtId(artEntity.getId());
            artPhotoEntity.setPhotoUrl(fileService.saveFile(newPhotos.get(i)));

            if (artChangeDTO.getChangeMainPhoto() && i == 0){
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
    }

    public void deleteArt(IntIdRequestDTO intIdRequestDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
        UUID artistId = customerEntity.getArtistId();
        ArtEntity artEntity = artRepository.findById(intIdRequestDTO.getId()).orElseThrow(BadCredentialsException::new);

        if (!artistId.equals(artEntity.getArtistId())) {
            throw new ForbiddenActionException();
        }

        notificationRepository.deleteAllBySubjectId(artEntity.getId());
        cartRepository.deleteAllByArtId(artEntity.getId());
        orderRepository.deleteAllByArtId(artEntity.getId());
        artPrivateSubscriptionRepository.deleteAllByArtId(artEntity.getId());

        artPhotoRepository.findAllByArtId(artEntity.getId()).stream()
                .map(ArtPhotoEntity::getPhotoUrl)
                .forEach(fileService::deleteFile);

        artPhotoRepository.deleteAllByArtId(artEntity.getId());
        artRepository.deleteById(artEntity.getId());
    }

    public List<ArtistArtDTO> getArtistArt(UUID artistId, String currentId) {
        if (!artistRepository.existsById(artistId)) {
            throw new UserNotFoundException();
        }

        List<ArtEntity> artEntities = artRepository.findAllByArtistId(artistId);
        List<ArtistArtDTO> dtos = new ArrayList<>();

        for (ArtEntity artEntity: artEntities) {
            ArtistArtDTO dto = new ArtistArtDTO();
            dto.setArtId(artEntity.getId());
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
                    dto.setAvailable(customerPrivateSubscriptionRepository.existsByCustomerIdAndPrivateSubscriptionId(customerId, artPrivSub.get().getSubscriptionId()) ||
                            customerEntity.getArtistId().equals(artistId));
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
        if (!customerRepository.existsById(customerId)) {
            throw new UserNotFoundException();
        }

        List<ArtEntity> artEntities = artRepository.findAllByOwnerId(customerId);
        List<CustomerArtDTO> dtos = new ArrayList<>();

        for (ArtEntity artEntity: artEntities) {
            CustomerArtDTO dto = new CustomerArtDTO();
            dto.setArtId(artEntity.getId());
            dto.setName(artEntity.getName());
            dto.setPrice(artEntity.getPrice());

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

        List<ArtEntity> entities = artRepository.findAllByType(artType).stream()
                .filter(art -> !artPrivateSubscriptionRepository.existsByArtId(art.getId()) || art.getSold())
                .toList();

        List<CommonArtDTO> dtos = new ArrayList<>();

        for (ArtEntity entity: entities) {
            dtos.add(commonArtDTOByEntity(entity));
        }

        return dtos;
    }

    public List<CommonArtDTO> searchPaintings(String input) {
        List<ArtEntity> artEntities = artRepository.findAll().stream()
                .filter(art -> !artPrivateSubscriptionRepository.existsByArtId(art.getId()) || art.getSold())
                .filter(art -> art.getType().equals("PAINTING"))
                .filter(art -> art.getName().toUpperCase().contains(input.toUpperCase()))
                .toList();

        List<CommonArtDTO> dtos = new ArrayList<>();

        for (ArtEntity art: artEntities) {
            dtos.add(commonArtDTOByEntity(art));
        }
        return dtos;
    }

    public List<CommonArtDTO> searchPhotos(String input) {
        List<ArtEntity> artEntities = artRepository.findAll().stream()
                .filter(art -> !artPrivateSubscriptionRepository.existsByArtId(art.getId()) || art.getSold())
                .filter(art -> art.getType().equals("PHOTO"))
                .filter(art -> art.getName().toUpperCase().contains(input.toUpperCase()))
                .toList();

        List<CommonArtDTO> dtos = new ArrayList<>();

        for (ArtEntity art: artEntities) {
            dtos.add(commonArtDTOByEntity(art));
        }
        return dtos;
    }

    public List<CommonArtDTO> searchSculptures(String input) {
        List<ArtEntity> artEntities = artRepository.findAll().stream()
                .filter(art -> !artPrivateSubscriptionRepository.existsByArtId(art.getId()) || art.getSold())
                .filter(art -> art.getType().equals("SCULPTURE"))
                .filter(art -> art.getName().toUpperCase().contains(input.toUpperCase()))
                .toList();

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

        ArtistEntity artistEntity = artistRepository.findById(entity.getArtistId()).get();

        dto.setArtistId(artistEntity.getId());
        dto.setArtistName(artistEntity.getArtistName());

        if(artPrivateSubscriptionRepository.existsByArtId(entity.getId())) {
            dto.setIsPrivate(true);
        } else {
            dto.setIsPrivate(false);
        }

        if (entity.getOwnerId() == null) {
            dto.setCustomerId(null);
            dto.setCustomerName(null);
            dto.setAvatarUrl(null);
        } else {
            CustomerEntity customerEntity = customerRepository.findById(entity.getOwnerId()).get();
            dto.setCustomerId(customerEntity.getId());
            dto.setCustomerName(customerEntity.getCustomerName());
            dto.setAvatarUrl(customerEntity.getAvatarUrl());
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
