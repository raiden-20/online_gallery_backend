package ru.vsu.cs.sheina.online_gallery_backend.service;

import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.C;
import org.springframework.security.core.parameters.P;
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

    public void movePrivatePaintings(Integer subscriptionId) {
        artPrivateSubscriptionRepository.deleteAllBySubscriptionId(subscriptionId);
    }

    public void createArt(String name, String type, List<MultipartFile> photos, String isPrivate, String price,
                          String description, String size, String frame, List<String> tags, List<String> materials,
                          String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
        UUID artistId = customerEntity.getArtistId();

        if (!artistRepository.existsById(artistId)) {
            throw new UserNotFoundException();
        }

        if (!type.equals("PAINTING") && !type.equals("PHOTO") && !type.equals("SCULPTURE")) {
            throw new BadCredentialsException();
        }

        ArtEntity artEntity = new ArtEntity();
        artEntity.setName(name);
        artEntity.setType(type);
        artEntity.setPrice(Double.parseDouble(price));
        artEntity.setDescription(description);
        artEntity.setSize(size);
        artEntity.setFrame(Boolean.getBoolean(frame));
        artEntity.setTags(tags);
        artEntity.setMaterials(materials);
        artEntity.setOwnerId(null);
        artRepository.save(artEntity);

        boolean privateArt = Boolean.getBoolean(isPrivate);

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
    }

    public ArtFullDTO getArt(Integer artId, String token) {
        ArtEntity artEntity = artRepository.findById(artId).orElseThrow(BadCredentialsException::new);

        ArtFullDTO artFullDTO = new ArtFullDTO();
        artFullDTO.setName(artEntity.getName());
        artFullDTO.setType(artEntity.getType());
        artFullDTO.setPrice(artEntity.getPrice());
        artFullDTO.setArtistId(artEntity.getArtistId());
        artFullDTO.setDescription(artEntity.getDescription());
        artFullDTO.setSize(artEntity.getSize());
        artFullDTO.setCreateDate(artEntity.getCreateDate());
        artFullDTO.setTags(artEntity.getTags());
        artFullDTO.setMaterials(artEntity.getMaterials());
        artFullDTO.setFrame(artEntity.getFrame());
        artFullDTO.setPublishDate(artEntity.getPublishDate());

        if (token.isEmpty() && artPrivateSubscriptionRepository.existsByArtId(artId)) {
            throw new ForbiddenActionException();
        } else if (!token.isEmpty() && !artPrivateSubscriptionRepository.existsByArtId(artId)) {
            artFullDTO.setStatus("AVAILABLE");
        } else if (!token.isEmpty() && artPrivateSubscriptionRepository.existsByArtId(artId)){
            UUID customerId = jwtParser.getIdFromAccessToken(token);
            PrivateSubscriptionEntity privateSubscriptionEntity = privateSubscriptionRepository.findByArtistId(artEntity.getArtistId()).get();
            if (!customerPrivateSubscriptionRepository.existsByCustomerIdAndPrivateSubscriptionId(customerId, privateSubscriptionEntity.getId())) {
                throw new ForbiddenActionException();
            }
        }

        if (artEntity.getOwnerId() != null) {
            artFullDTO.setCustomerId(artEntity.getOwnerId());
            CustomerEntity customerEntity = customerRepository.findById(artEntity.getOwnerId()).orElseThrow(UserNotFoundException::new);
            artFullDTO.setCustomerName(customerEntity.getCustomerName());
            artFullDTO.setStatus("SOLD");
        } else {
            artFullDTO.setCustomerName(null);
            artFullDTO.setCustomerId(null);
        }

        if (token.isEmpty() && artEntity.getOwnerId() == null) {
            artFullDTO.setStatus("AVAILABLE");
        } else if (!token.isEmpty() && artEntity.getOwnerId() == null) {
            UUID customerId = jwtParser.getIdFromAccessToken(token);
            if (cartRepository.existByCustomerIdAndArtId(customerId, artId)) {
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

        return artFullDTO;
    }

    public void changeArt(String artId, String name, String type, List<MultipartFile> newPhotos, List<String> deletePhotoUrls,
                          String changeMainPhoto, String isPrivate, String price, String description, String size, String frame,
                          List<String> tags, List<String> materials, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
        UUID artistId = customerEntity.getArtistId();

        if (!artistRepository.existsById(artistId)) {
            throw new UserNotFoundException();
        }

        if (!type.equals("PAINTING") && !type.equals("PHOTO") && !type.equals("SCULPTURE")) {
            throw new BadCredentialsException();
        }

        ArtEntity artEntity = artRepository.findById(Integer.valueOf(artId)).orElseThrow(BadCredentialsException::new);

        if (artistId != artEntity.getArtistId()) {
            throw new ForbiddenActionException();
        }

        artEntity.setName(name);
        artEntity.setType(type);
        artEntity.setPrice(Double.parseDouble(price));
        artEntity.setDescription(description);
        artEntity.setSize(size);
        artEntity.setFrame(Boolean.valueOf(frame));
        artEntity.setTags(tags);
        artEntity.setMaterials(materials);

        for (String url: deletePhotoUrls) {
            fileService.deleteFile(url);
            artPhotoRepository.deleteAllByArtIdAndAndPhotoUrl(artEntity.getId(), url);
        }

        for (int i = 0; i < newPhotos.size(); i++) {
            ArtPhotoEntity artPhotoEntity = new ArtPhotoEntity();
            artPhotoEntity.setArtId(artEntity.getId());
            artPhotoEntity.setPhotoUrl(fileService.saveFile(newPhotos.get(i)));

            if (Boolean.getBoolean(changeMainPhoto) & i == 0){
                Optional<ArtPhotoEntity> mainPhoto = artPhotoRepository.findByArtIdAndAndDefaultPhoto(artEntity.getId(), true);
                if (mainPhoto.isPresent()) {
                    ArtPhotoEntity mainPhotoEntity = mainPhoto.get();
                    mainPhotoEntity.setDefaultPhoto(false);
                    artPhotoRepository.save(mainPhotoEntity);
                }
                artPhotoEntity.setDefaultPhoto(true);
            }

            artPhotoEntity.setDefaultPhoto(false);
            artPhotoRepository.save(artPhotoEntity);
        }

        if (Boolean.getBoolean(isPrivate) && !privateSubscriptionRepository.existsByArtistId(artistId)) {
            throw new BadActionException("You don't have a private subscription");
        } else if (Boolean.getBoolean(isPrivate) && !artPrivateSubscriptionRepository.existsByArtId(artEntity.getId())){
            PrivateSubscriptionEntity subscription = privateSubscriptionRepository.findByArtistId(artistId).get();
            ArtPrivateSubscriptionEntity artPrivateSubscription = new ArtPrivateSubscriptionEntity();
            artPrivateSubscription.setArtId(artEntity.getId());
            artPrivateSubscription.setSubscriptionId(subscription.getId());
            artPrivateSubscriptionRepository.save(artPrivateSubscription);
        } else if (!Boolean.getBoolean(isPrivate) && artPrivateSubscriptionRepository.existsByArtId(artEntity.getId())) {
            artPrivateSubscriptionRepository.deleteAllByArtId(artEntity.getId());
        }
    }

    public void deleteArt(IntIdRequestDTO intIdRequestDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
        UUID artistId = customerEntity.getArtistId();
        ArtEntity artEntity = artRepository.findById(intIdRequestDTO.getId()).orElseThrow(BadCredentialsException::new);

        if (artEntity.getArtistId() != artistId) {
            throw new ForbiddenActionException();
        }

        artPrivateSubscriptionRepository.deleteAllByArtId(artEntity.getId());

        artPhotoRepository.findAllByArtId(artEntity.getId()).stream()
                .map(ArtPhotoEntity::getPhotoUrl)
                .forEach(fileService::deleteFile);

        artPhotoRepository.deleteAllByArtId(artEntity.getId());
        artRepository.deleteById(artEntity.getId());
    }

    public List<ArtistArtDTO> getArtistArt(UUID artistId, String token) {
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
                CustomerEntity customerEntity = customerRepository.findById(artEntity.getArtistId()).orElseThrow(UserNotFoundException::new);
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
                if (!token.isEmpty()) {
                    UUID customerId = jwtParser.getIdFromAccessToken(token);
                    if (customerPrivateSubscriptionRepository.existsByCustomerIdAndPrivateSubscriptionId(customerId, artPrivSub.get().getSubscriptionId())) {
                        dto.setAvailable(true);
                    } else {
                        dto.setAvailable(false);
                    }
                } else {
                    dto.setAvailable(false);
                }
            } else {
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

        List<ArtEntity> entities = artRepository.findAllByType(type.toUpperCase().substring(0, type.length() - 1))
                .stream().filter(art -> !artPrivateSubscriptionRepository.existsByArtId(art.getId()))
                .toList();

        List<CommonArtDTO> dtos = new ArrayList<>();

        for (ArtEntity entity: entities) {
            CommonArtDTO dto = new CommonArtDTO();
            dto.setArtId(entity.getId());
            dto.setName(entity.getName());
            dto.setPrice(entity.getPrice());

            ArtistEntity artistEntity = artistRepository.findById(entity.getArtistId()).get();

            dto.setArtistId(artistEntity.getId());
            dto.setArtistName(artistEntity.getArtistName());

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

            dtos.add(dto);
        }

        return dtos;
    }

    public List<ArtShortDTO> searchArt(String input) {
        List<ArtEntity> artEntities = artRepository.findAll().stream()
                .filter(art -> !artPrivateSubscriptionRepository.existsByArtId(art.getId()))
                .filter(art -> art.getName().toUpperCase().contains(input.toUpperCase()))
                .toList();

        List<ArtShortDTO> dtos = new ArrayList<>();

        for (ArtEntity art: artEntities) {
            ArtShortDTO dto = new ArtShortDTO();
            dto.setArtId(art.getId());
            dto.setName(art.getName());
            dto.setPrice(art.getPrice());
            dto.setArtistId(art.getArtistId());

            Optional<ArtPhotoEntity> artPhotoEntity = artPhotoRepository.findByArtIdAndAndDefaultPhoto(art.getId(), true);
            if (artPhotoEntity.isPresent()) {
                dto.setPhotoUrl(artPhotoEntity.get().getPhotoUrl());
            } else {
                dto.setPhotoUrl("");
            }

            ArtistEntity artistEntity = artistRepository.findById(art.getArtistId()).orElseThrow(UserNotFoundException::new);
            dto.setArtistName(artistEntity.getArtistName());

            dtos.add(dto);
        }

        return dtos;
    }
}
