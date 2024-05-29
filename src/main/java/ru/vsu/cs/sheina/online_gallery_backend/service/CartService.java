package ru.vsu.cs.sheina.online_gallery_backend.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.sheina.online_gallery_backend.dto.art.ArtShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.order.PurchaseDTO;
import ru.vsu.cs.sheina.online_gallery_backend.entity.*;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadActionException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentialsException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.UserNotFoundException;
import ru.vsu.cs.sheina.online_gallery_backend.repository.*;
import ru.vsu.cs.sheina.online_gallery_backend.utils.JWTParser;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CartService {

    private final JWTParser jwtParser;
    private final CartRepository cartRepository;
    private final ArtRepository artRepository;
    private final ArtistRepository artistRepository;
    private final ArtPhotoRepository artPhotoRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OrderService orderService;
    private final NotificationService notificationService;

    public void addArt(IntIdRequestDTO intIdRequestDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);

        if (!artRepository.existsById(intIdRequestDTO.getId())) {
            throw new BadCredentialsException();
        }

        if (cartRepository.existsByCustomerIdAndSubjectId(customerId, intIdRequestDTO.getId())) {
            throw new BadActionException("Art has already been added to the cart");
        }

        CartEntity cartEntity = new CartEntity();
        cartEntity.setCustomerId(customerId);
        cartEntity.setSubjectId(intIdRequestDTO.getId());
        cartRepository.save(cartEntity);
    }

    public void deleteArtFromCart(IntIdRequestDTO intIdRequestDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);

        if (!cartRepository.existsByCustomerIdAndSubjectId(customerId, intIdRequestDTO.getId())) {
            throw new BadActionException("You can't do this action");
        }

        CartEntity cartEntity = cartRepository.findByCustomerIdAndSubjectId(customerId, intIdRequestDTO.getId()).get();

        cartRepository.delete(cartEntity);
    }

    public List<ArtShortDTO> getCartData(String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);

        List<Integer> artIds = cartRepository.findAllByCustomerId(customerId).stream()
                .map(CartEntity::getSubjectId)
                .toList();
        List<ArtShortDTO> dtos = new ArrayList<>();

        for (Integer artId: artIds) {
            ArtShortDTO dto = new ArtShortDTO();
            ArtEntity artEntity = artRepository.findById(artId).get();

            dto.setArtId(artId);
            dto.setName(artEntity.getName());
            dto.setPrice(artEntity.getPrice());
            dto.setArtistId(artEntity.getArtistId());

            ArtistEntity artistEntity = artistRepository.findById(artEntity.getArtistId()).orElseThrow(UserNotFoundException::new);
            dto.setArtistName(artistEntity.getArtistName());

            Optional<ArtPhotoEntity> artPhotoOpt = artPhotoRepository.findByArtIdAndAndDefaultPhoto(artId, true);
            if (artPhotoOpt.isPresent()) {
                dto.setPhotoUrl(artPhotoOpt.get().getPhotoUrl());
            } else {
                dto.setPhotoUrl("");
            }
            dtos.add(dto);
        }
        return dtos;
    }

    public List<Integer> buy(PurchaseDTO purchaseDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);

        List<Integer> orderIds = new ArrayList<>();

        for (Integer artId: purchaseDTO.getArts().keySet()) {
            Integer orderId = orderService.createOrder(artId, customerId, purchaseDTO.getCardId(), purchaseDTO.getAddressId(), purchaseDTO.getArts().get(artId));
            orderIds.add(orderId);
            cartRepository.deleteAllBySubjectId(artId);

            OrderEntity orderEntity = orderRepository.findById(orderId).get();
            CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
            ArtEntity artEntity = artRepository.findById(artId).orElseThrow(BadCredentialsException::new);

            notificationService.sendArtSoldNotification(orderEntity, customerEntity, artEntity);
        }

        return orderIds;
    }
}
