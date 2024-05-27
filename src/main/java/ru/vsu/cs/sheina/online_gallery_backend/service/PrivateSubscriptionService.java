package ru.vsu.cs.sheina.online_gallery_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.sheina.online_gallery_backend.dto.customer.CustomerShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.UUIDRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.subscription.PriceDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.subscription.PrivateSubscriptionDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.subscription.SubscribeDTO;
import ru.vsu.cs.sheina.online_gallery_backend.entity.*;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadActionException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentialsException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.UserNotFoundException;
import ru.vsu.cs.sheina.online_gallery_backend.repository.*;
import ru.vsu.cs.sheina.online_gallery_backend.utils.JWTParser;

import java.sql.Timestamp;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PrivateSubscriptionService {

    private final PrivateSubscriptionRepository privateSubscriptionRepository;
    private final CardRepository cardRepository;
    private final ArtService artService;
    private final CustomerPrivateSubscriptionRepository customerPrivateSubscriptionRepository;
    private final CustomerRepository customerRepository;
    private final ArtistRepository artistRepository;
    private final JWTParser jwtParser;
    private final NotificationService notificationService;

    private final Integer DAYS_BETWEEN_PAYMENT = 30;

    public void subscribe(SubscribeDTO subscribeDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        UUID artistId = subscribeDTO.getArtistId();

        if (!customerRepository.existsById(customerId) || !artistRepository.existsById(artistId)) {
            throw new UserNotFoundException();
        }

        if (!cardRepository.existsById(subscribeDTO.getCardId())) {
            throw new BadCredentialsException();
        }

        PrivateSubscriptionEntity subscription = privateSubscriptionRepository.findByArtistId(artistId).orElseThrow(BadCredentialsException::new);

        if (customerPrivateSubscriptionRepository.existsByCustomerIdAndPrivateSubscriptionId(customerId, subscription.getId())) {
            throw new BadActionException("Subscription already exists");
        }

        CustomerPrivateSubscriptionEntity cusPrivSubEntity = new CustomerPrivateSubscriptionEntity();
        cusPrivSubEntity.setPrivateSubscriptionId(subscription.getId());
        cusPrivSubEntity.setCustomerId(customerId);
        cusPrivSubEntity.setCardId(subscribeDTO.getCardId());
        cusPrivSubEntity.setCreateDate(new Timestamp(System.currentTimeMillis() + 3 * 60 * 60 * 1000));

        Calendar cal = Calendar.getInstance();
        cal.setTime(cusPrivSubEntity.getCreateDate());
        cal.add(Calendar.DAY_OF_WEEK, DAYS_BETWEEN_PAYMENT);

        cusPrivSubEntity.setPaymentDate(new Timestamp(cal.getTime().getTime()));

        customerPrivateSubscriptionRepository.save(cusPrivSubEntity);
    }

    public void unsubscribe(UUIDRequestDTO uuidRequestDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        UUID artistId = uuidRequestDTO.getId();

        if (!customerRepository.existsById(customerId)) {
            throw new UserNotFoundException();
        }

        PrivateSubscriptionEntity privateSubscriptionEntity = privateSubscriptionRepository.findByArtistId(artistId).orElseThrow(BadCredentialsException::new);

        if (!customerPrivateSubscriptionRepository.existsByCustomerIdAndPrivateSubscriptionId(customerId, privateSubscriptionEntity.getId())) {
            throw new BadActionException("You don't have a subscription");
        }

        customerPrivateSubscriptionRepository.deleteByCustomerIdAndPrivateSubscriptionId(customerId, privateSubscriptionEntity.getId());
    }

    public List<PrivateSubscriptionDTO> getSubscriptions(String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);

        if (!customerRepository.existsById(customerId)) {
            throw new UserNotFoundException();
        }

        List<CustomerPrivateSubscriptionEntity> cusPrivSubEntities = customerPrivateSubscriptionRepository.findAllByCustomerId(customerId);
        List<PrivateSubscriptionDTO> dtos = new ArrayList<>();

        for (CustomerPrivateSubscriptionEntity entity: cusPrivSubEntities) {
            PrivateSubscriptionEntity privateSubscriptionEntity = privateSubscriptionRepository.findById(entity.getPrivateSubscriptionId()).orElseThrow(BadCredentialsException::new);
            ArtistEntity artistEntity = artistRepository.findById(privateSubscriptionEntity.getArtistId()).orElseThrow(UserNotFoundException::new);

            if (entity.getPaymentDate().before(new Timestamp(System.currentTimeMillis() + 3 * 60 * 60 * 1000))) {
                entity.setPaymentDate(changePaymentDate(entity));
                customerPrivateSubscriptionRepository.save(entity);
            }

            PrivateSubscriptionDTO dto = new PrivateSubscriptionDTO();
            dto.setPayDate(entity.getPaymentDate());
            dto.setArtistId(privateSubscriptionEntity.getArtistId());
            dto.setPrice(privateSubscriptionEntity.getPrice());
            dto.setArtistName(artistEntity.getArtistName());
            dto.setAvatarUrl(artistEntity.getAvatarUrl());

            dtos.add(dto);

        }
        return dtos;
    }

    public void createSubscription(PriceDTO priceDTO) {
        if (!artistRepository.existsById(priceDTO.getArtistId())) {
            throw new UserNotFoundException();
        }

        if (privateSubscriptionRepository.existsByArtistId(priceDTO.getArtistId())) {
            throw new BadActionException("You already have a subscription");
        }

        PrivateSubscriptionEntity entity = new PrivateSubscriptionEntity();
        entity.setArtistId(priceDTO.getArtistId());
        entity.setPrice(priceDTO.getPrice());
        entity.setCreateDate(new Timestamp(System.currentTimeMillis() + 3 * 60 * 60 * 1000));

        privateSubscriptionRepository.save(entity);
    }

    public List<CustomerShortDTO> getSubscribers(String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
        PrivateSubscriptionEntity subscriptionEntity = privateSubscriptionRepository.findByArtistId(customerEntity.getArtistId()).orElseThrow(BadCredentialsException::new);

        return customerPrivateSubscriptionRepository.findAllByPrivateSubscriptionId(subscriptionEntity.getId()).stream()
                .map(CustomerPrivateSubscriptionEntity::getCustomerId)
                .map(customerRepository::findById)
                .map(Optional::get)
                .map(ent -> new CustomerShortDTO(ent.getId(), ent.getCustomerName(), ent.getAvatarUrl()))
                .toList();
    }

    public PriceDTO getSubscriptionData(UUID artistId) {
        PrivateSubscriptionEntity entity = privateSubscriptionRepository.findByArtistId(artistId).orElseThrow(BadCredentialsException::new);

        return new PriceDTO(artistId, entity.getPrice());
    }

    public void deleteSubscription(String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
        UUID artistId = customerEntity.getArtistId();
        ArtistEntity artistEntity = artistRepository.findById(artistId).orElseThrow(UserNotFoundException::new);
        PrivateSubscriptionEntity subscriptionEntity = privateSubscriptionRepository.findByArtistId(artistId).orElseThrow(BadCredentialsException::new);

        notificationService.sendPrivateDeletedNotification(artistEntity, subscriptionEntity);

        customerPrivateSubscriptionRepository.deleteAllByPrivateSubscriptionId(subscriptionEntity.getId());
        artService.movePrivatePaintings(subscriptionEntity.getId());
        privateSubscriptionRepository.deleteById(subscriptionEntity.getId());
    }

    public List<CustomerShortDTO> searchCustomerUsers(String input, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
        PrivateSubscriptionEntity subscriptionEntity = privateSubscriptionRepository.findByArtistId(customerEntity.getArtistId()).orElseThrow(BadCredentialsException::new);

        return customerPrivateSubscriptionRepository.findAllByPrivateSubscriptionId(subscriptionEntity.getId()).stream()
                .map(CustomerPrivateSubscriptionEntity::getCustomerId)
                .map(customerRepository::findById)
                .map(Optional::get)
                .filter(ent -> ent.getCustomerName().toUpperCase().contains(input.toUpperCase()))
                .map(ent -> new CustomerShortDTO(ent.getId(), ent.getCustomerName(), ent.getAvatarUrl()))
                .toList();
    }

    public List<PrivateSubscriptionDTO> searchArtistUsers(String input, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);

        if (!customerRepository.existsById(customerId)) {
            throw new UserNotFoundException();
        }

        List<CustomerPrivateSubscriptionEntity> cusPrivSubEntities = customerPrivateSubscriptionRepository.findAllByCustomerId(customerId);
        List<PrivateSubscriptionDTO> dtos = new ArrayList<>();

        for (CustomerPrivateSubscriptionEntity entity: cusPrivSubEntities) {
            PrivateSubscriptionEntity privateSubscriptionEntity = privateSubscriptionRepository.findById(entity.getPrivateSubscriptionId()).orElseThrow(BadCredentialsException::new);
            ArtistEntity artistEntity = artistRepository.findById(privateSubscriptionEntity.getArtistId()).orElseThrow(UserNotFoundException::new);

            if (artistEntity.getArtistName().toUpperCase().contains(input.toUpperCase())) {
                if (entity.getPaymentDate().before(new Timestamp(System.currentTimeMillis() + 3 * 60 * 60 * 1000))) {
                    changePaymentDate(entity);
                }

                PrivateSubscriptionDTO dto = new PrivateSubscriptionDTO();
                dto.setPayDate(entity.getPaymentDate());
                dto.setArtistId(privateSubscriptionEntity.getArtistId());
                dto.setPrice(privateSubscriptionEntity.getPrice());
                dto.setArtistName(artistEntity.getArtistName());
                dto.setAvatarUrl(artistEntity.getAvatarUrl());

                dtos.add(dto);
            }
        }
        return dtos;
    }

    private Timestamp changePaymentDate(CustomerPrivateSubscriptionEntity entity) {
        Timestamp currentDate = new Timestamp(System.currentTimeMillis() + 3 * 60 * 60 * 1000);
        Timestamp paymentDate = entity.getPaymentDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(entity.getCreateDate());

        while (currentDate.after(paymentDate)) {
            cal.add(Calendar.DAY_OF_WEEK, DAYS_BETWEEN_PAYMENT);
            paymentDate = new Timestamp(cal.getTime().getTime());
        }

        return paymentDate;
    }
}
