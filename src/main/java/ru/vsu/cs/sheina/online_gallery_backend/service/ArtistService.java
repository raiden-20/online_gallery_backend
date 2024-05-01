package ru.vsu.cs.sheina.online_gallery_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.sheina.online_gallery_backend.dto.artist.ArtistArtDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.artist.ArtistFullDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.artist.ArtistRegistrationDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.artist.ArtistShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.entity.*;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.UserAlreadyExistsException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.UserNotFoundException;
import ru.vsu.cs.sheina.online_gallery_backend.repository.*;
import ru.vsu.cs.sheina.online_gallery_backend.utils.JWTParser;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final CustomerRepository customerRepository;
    private final ArtPrivateSubscriptionRepository artPrivateSubscriptionRepository;
    private final PostRepository postRepository;
    private final PostPhotoRepository postPhotoRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ArtRepository artRepository;
    private final ArtPhotoRepository artPhotoRepository;
    private final PublicSubscriptionRepository publicSubscriptionRepository;
    private final CustomerPrivateSubscriptionRepository customerPrivateSubscriptionRepository;
    private final PrivateSubscriptionRepository privateSubscriptionRepository;
    private final FileService fileService;
    private final JWTParser jwtParser;

    public ArtistFullDTO getArtistData(UUID artistId, String currentId) {
        ArtistEntity artistEntity = artistRepository.findById(artistId).orElseThrow(UserNotFoundException::new);
        CustomerEntity customerEntity = customerRepository.findByArtistId(artistId).orElseThrow(UserNotFoundException::new);

        int views = artistEntity.getViews();
        artistEntity.setViews(++views);
        artistRepository.save(artistEntity);

        ArtistFullDTO dto = new ArtistFullDTO();

        dto.setArtistName(artistEntity.getArtistName());
        dto.setDescription(artistEntity.getDescription());
        dto.setAvatarUrl(artistEntity.getAvatarUrl());
        dto.setCoverUrl(artistEntity.getCoverUrl());
        dto.setCustomerId(customerEntity.getId());

        Optional<PrivateSubscriptionEntity> privateSubscriptionOpt = privateSubscriptionRepository.findByArtistId(artistId);

        if (!currentId.equals("null")) {
            UUID customerId = UUID.fromString(currentId);
            dto.setIsPublicSubscribe(publicSubscriptionRepository.existsByArtistIdAndCustomerId(artistId, customerId));
            privateSubscriptionOpt.ifPresent(subscriptionEntity -> dto.setIsPrivateSubscribe(customerPrivateSubscriptionRepository.existsByCustomerIdAndPrivateSubscriptionId(customerId,
                    subscriptionEntity.getId())));
        } else {
            dto.setIsPrivateSubscribe(false);
            dto.setIsPublicSubscribe(false);
        }

        List<ArtEntity> arts = artRepository.findAllByArtistId(artistId);

        int countSoldArts = 0;
        Double salesAmount = 0.0;

        for (ArtEntity art: arts) {
            if (art.getSold()) {
                countSoldArts++;
                salesAmount += art.getPrice();
            }
        }

        dto.setCountSoldArts(countSoldArts);
        dto.setSalesAmount(salesAmount);

        if (privateSubscriptionOpt.isPresent()) {
            dto.setCountSubscribers(customerPrivateSubscriptionRepository.countByPrivateSubscriptionId(privateSubscriptionOpt.get().getId()));
        } else {
            dto.setCountSubscribers(null);
        }

        return dto;
    }

    public void setArtistData(String token, String artistName, String avatarUrl, String coverUrl, String description, MultipartFile avatar, MultipartFile cover) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        UUID artistId = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new).getArtistId();

        ArtistEntity artistEntity = artistRepository.findById(artistId).orElseThrow(UserNotFoundException::new);

        artistEntity.setArtistName(artistName);
        artistEntity.setDescription(description);

        if (!avatar.isEmpty()){
            if (!artistEntity.getAvatarUrl().isEmpty()) {
                fileService.deleteFile(artistEntity.getAvatarUrl());
            }
            String url = fileService.saveFile(avatar);
            artistEntity.setAvatarUrl(url);
        } else if (avatarUrl.equals("delete") && avatar.isEmpty()) {
            fileService.deleteFile(artistEntity.getAvatarUrl());
            artistEntity.setAvatarUrl("");
        }

        if (!cover.isEmpty()){
            if (!artistEntity.getCoverUrl().isEmpty()) {
                fileService.deleteFile(artistEntity.getCoverUrl());
            }
            String url = fileService.saveFile(cover);
            artistEntity.setCoverUrl(url);
        } else if (coverUrl.equals("delete") && cover.isEmpty()) {
            fileService.deleteFile(artistEntity.getCoverUrl());
            artistEntity.setCoverUrl("");
        }

        artistRepository.save(artistEntity);
    }

    public UUID createArtist(ArtistRegistrationDTO artistRegistrationDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);

        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
        if (customerEntity.getArtistId() != null){
            throw new UserAlreadyExistsException();
        }

        ArtistEntity artistEntity = new ArtistEntity();
        artistEntity.setId(UUID.randomUUID());
        artistEntity.setArtistName(artistRegistrationDTO.getArtistName());
        artistEntity.setDescription("");
        artistEntity.setAvatarUrl("");
        artistEntity.setCoverUrl("");
        artistEntity.setViews(0);

        artistRepository.save(artistEntity);

        customerEntity.setArtistId(artistEntity.getId());

        customerRepository.save(customerEntity);

        return artistEntity.getId();
    }

    public List<ArtistArtDTO> getArtists() {
        List<ArtistArtDTO> dtos = artistRepository.findAll().stream()
                .map(art -> new ArtistArtDTO(art.getId(), art.getArtistName(), art.getAvatarUrl(), art.getViews(), null))
                .toList();
        for (ArtistArtDTO dto: dtos) {
            Map<Integer, String> arts = new HashMap<>();
            List<ArtEntity> artEntities = artRepository.findAllByArtistId(dto.getArtistId());
            for (ArtEntity artEntity: artEntities) {
                Optional<ArtPhotoEntity> defaultPhoto = artPhotoRepository.findByArtIdAndAndDefaultPhoto(artEntity.getId(), true);
                if (defaultPhoto.isPresent()){
                    arts.put(artEntity.getId(), defaultPhoto.get().getPhotoUrl());
                } else {
                    arts.put(artEntity.getId(), "");
                }
                if (arts.size() >= 5) {
                    break;
                }
            }
            dto.setArts(arts);
        }

        return dtos;
    }

    public List<ArtistShortDTO> searchArtist(String input) {
        return artistRepository.findAll().stream()
                .filter(art -> art.getArtistName().toUpperCase().contains(input.toUpperCase()))
                .map(art -> new ArtistShortDTO(art.getId(), art.getArtistName(), art.getAvatarUrl(), art.getViews()))
                .toList();
    }

    public void deleteAccount(UUID artistId) {
        ArtistEntity artistEntity = artistRepository.findById(artistId).orElseThrow(UserNotFoundException::new);
        List<ArtEntity> artEntities = artRepository.findAllByArtistId(artistId);
        for (ArtEntity art: artEntities) {
            cartRepository.deleteAllByArtId(art.getId());
            orderRepository.deleteAllByArtId(art.getId());
            artPrivateSubscriptionRepository.deleteAllByArtId(art.getId());

            artPhotoRepository.findAllByArtId(art.getId()).stream()
                    .map(ArtPhotoEntity::getPhotoUrl)
                    .forEach(fileService::deleteFile);

            artPhotoRepository.deleteAllByArtId(art.getId());
            artRepository.deleteById(art.getId());
        }

        publicSubscriptionRepository.deleteAllByArtistId(artistId);
        postRepository.findAllByArtistId(artistId)
                .forEach(ent -> postPhotoRepository.deleteAllByPostId(ent.getId()));
        postRepository.deleteAllByArtistId(artistId);

        if (privateSubscriptionRepository.existsByArtistId(artistId)) {
            PrivateSubscriptionEntity privSubEntity = privateSubscriptionRepository.findByArtistId(artistId).get();
            customerPrivateSubscriptionRepository.deleteAllByPrivateSubscriptionId(privSubEntity.getId());
            artPrivateSubscriptionRepository.deleteAllBySubscriptionId(privSubEntity.getId());
            privateSubscriptionRepository.deleteById(privSubEntity.getId());
        }
    }
}
