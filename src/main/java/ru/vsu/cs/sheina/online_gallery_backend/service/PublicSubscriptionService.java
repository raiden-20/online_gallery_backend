package ru.vsu.cs.sheina.online_gallery_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.sheina.online_gallery_backend.dto.artist.ArtistShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.customer.CustomerShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.UUIDRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.entity.CustomerEntity;
import ru.vsu.cs.sheina.online_gallery_backend.entity.PublicSubscriptionEntity;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadActionException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BlockUserException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.UserNotFoundException;
import ru.vsu.cs.sheina.online_gallery_backend.repository.ArtistRepository;
import ru.vsu.cs.sheina.online_gallery_backend.repository.BlockUserRepository;
import ru.vsu.cs.sheina.online_gallery_backend.repository.CustomerRepository;
import ru.vsu.cs.sheina.online_gallery_backend.repository.PublicSubscriptionRepository;
import ru.vsu.cs.sheina.online_gallery_backend.utils.JWTParser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublicSubscriptionService {

    private final PublicSubscriptionRepository publicSubscriptionRepository;
    private final CustomerRepository customerRepository;
    private final ArtistRepository artistRepository;
    private final JWTParser jwtParser;
    private final BlockUserRepository blockUserRepository;

    public void actionWithSubscription(UUIDRequestDTO uuidRequestDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);

        if (blockUserRepository.existsById(customerId)) {
            throw new BlockUserException();
        }

        UUID artistId = uuidRequestDTO.getId();

        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);

        if (!artistRepository.existsById(artistId)) {
            throw new UserNotFoundException();
        }

        if(customerEntity.getArtistId() == artistId) {
            throw new BadActionException("You can't subscribe to your account");
        }

        Optional<PublicSubscriptionEntity> optionalEntity = publicSubscriptionRepository.findByArtistIdAndCustomerId(artistId, customerId);

        if (optionalEntity.isPresent()) {
            publicSubscriptionRepository.deleteById(optionalEntity.get().getId());
        } else {
            PublicSubscriptionEntity entity = new PublicSubscriptionEntity();
            entity.setArtistId(artistId);
            entity.setCustomerId(customerId);
            publicSubscriptionRepository.save(entity);
        }
    }

    public List<ArtistShortDTO> getSubscriptions(String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);

        if (blockUserRepository.existsById(customerId)) {
            throw new BlockUserException();
        }

        if (!customerRepository.existsById(customerId)) {
            throw new UserNotFoundException();
        }

        return publicSubscriptionRepository.findAllByCustomerId(customerId).stream()
            .map(PublicSubscriptionEntity::getArtistId)
            .map(artistRepository::findById)
            .map(Optional::get)
            .map(ent -> new ArtistShortDTO(ent.getId(), ent.getArtistName(), ent.getAvatarUrl(), ent.getViews()))
            .toList();
    }

    public List<CustomerShortDTO> getSubscribers(String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);

        if (blockUserRepository.existsById(customerId)) {
            throw new BlockUserException();
        }

        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);

        UUID artistId = customerEntity.getArtistId();

        if (artistId == null) {
            throw new UserNotFoundException();
        }

        return publicSubscriptionRepository.findAllByArtistId(artistId).stream()
                .map(PublicSubscriptionEntity::getCustomerId)
                .map(customerRepository::findById)
                .map(Optional::get)
                .map(ent -> new CustomerShortDTO(ent.getId(), ent.getCustomerName(), ent.getAvatarUrl()))
                .toList();
    }

    public List<CustomerShortDTO> searchCustomerUsers(String input, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);

        if (blockUserRepository.existsById(customerId)) {
            throw new BlockUserException();
        }

        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
        UUID artistId = customerEntity.getArtistId();

        if (artistId == null) {
            throw new UserNotFoundException();
        }

        return publicSubscriptionRepository.findAllByArtistId(artistId).stream()
                .map(PublicSubscriptionEntity::getCustomerId)
                .map(customerRepository::findById)
                .map(Optional::get)
                .filter(ent -> ent.getCustomerName().toUpperCase().contains(input.toUpperCase()))
                .map(ent -> new CustomerShortDTO(ent.getId(), ent.getCustomerName(), ent.getAvatarUrl()))
                .toList();
    }

    public List<ArtistShortDTO> searchArtistUsers(String input, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);

        if (blockUserRepository.existsById(customerId)) {
            throw new BlockUserException();
        }

        if (!customerRepository.existsById(customerId)) {
            throw new UserNotFoundException();
        }

        return publicSubscriptionRepository.findAllByCustomerId(customerId).stream()
                .map(PublicSubscriptionEntity::getArtistId)
                .map(artistRepository::findById)
                .map(Optional::get)
                .filter(ent -> ent.getArtistName().toUpperCase().contains(input.toUpperCase()))
                .map(ent -> new ArtistShortDTO(ent.getId(), ent.getArtistName(), ent.getAvatarUrl(), ent.getViews()))
                .toList();
    }
}
