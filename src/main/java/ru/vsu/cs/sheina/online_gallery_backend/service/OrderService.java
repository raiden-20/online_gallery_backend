package ru.vsu.cs.sheina.online_gallery_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.order.OrderDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.order.OrderShortDTO;
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
public class OrderService {

    private final OrderRepository orderRepository;
    private final ArtPhotoRepository artPhotoRepository;
    private final CustomerRepository customerRepository;
    private final JWTParser jwtParser;
    private final ArtRepository artRepository;
    private final ArtistRepository artistRepository;
    private final CardRepository cardRepository;
    private final AddressRepository addressRepository;
    
    public Integer createOrder(Integer artId, UUID customerId, Integer cardId, Integer addressId, Boolean anonim) {
        ArtEntity artEntity = artRepository.findById(artId).orElseThrow(BadCredentialsException::new);

        OrderEntity orderEntity = new OrderEntity();

        orderEntity.setCustomerId(customerId);
        orderEntity.setArtId(artId);
        orderEntity.setArtistId(artEntity.getArtistId());
        orderEntity.setStatus("CREATED");
        orderEntity.setArtistComment("");
        orderEntity.setCardId(cardId);
        orderEntity.setAddressId(addressId);

        orderRepository.save(orderEntity);

        artEntity.setOwnerId(customerId);
        artEntity.setSold(true);
        artRepository.save(artEntity);

        return orderEntity.getId();
    }

    public List<OrderDTO> getOrders(UUID userId, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        UUID artistId = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new).getArtistId();
        List<Integer> orderIds = new ArrayList<>();

        if (userId.equals(customerId)) {
            orderIds = orderRepository.findAllByCustomerId(customerId).stream()
                    .map(OrderEntity::getId)
                    .toList();
        } else if (userId.equals(artistId)) {
            orderIds = orderRepository.findAllByArtistId(artistId).stream()
                    .map(OrderEntity::getId)
                    .toList();
        } else {
            throw new ForbiddenActionException();
        }
        List<OrderDTO> orders = new ArrayList<>();

        for (Integer orderId: orderIds) {
            orders.add(getOrderDTObyId(orderId));
        }

        return orders;
    }

    public OrderDTO getOrder(Integer orderId, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        UUID artistId = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new).getArtistId();

        OrderEntity orderEntity = orderRepository.findById(orderId).orElseThrow(BadCredentialsException::new);

        if (!orderEntity.getArtistId().equals(artistId) || !orderEntity.getCustomerId().equals(customerId)) {
            throw new ForbiddenActionException();
        }

        return getOrderDTObyId(orderId);
    }

    public void receive(IntIdRequestDTO intIdDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);

        OrderEntity orderEntity = orderRepository.findById(intIdDTO.getId()).orElseThrow(BadCredentialsException::new);

        if (!orderEntity.getCustomerId().equals(customerId)) {
            throw new ForbiddenActionException();
        }

        orderEntity.setStatus("FINISHED");
        orderRepository.save(orderEntity);
    }

    public void send(OrderShortDTO orderShortDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        UUID artistId = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new).getArtistId();

        OrderEntity orderEntity = orderRepository.findById(orderShortDTO.getId()).orElseThrow(BadCredentialsException::new);

        if (!orderEntity.getArtistId().equals(artistId)) {
            throw new ForbiddenActionException();
        }

        orderEntity.setArtistComment(orderShortDTO.getComment());
        orderEntity.setStatus("PROGRESS");

        orderRepository.save(orderEntity);
    }

    public void edit(OrderShortDTO orderShortDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        UUID artistId = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new).getArtistId();

        OrderEntity orderEntity = orderRepository.findById(orderShortDTO.getId()).orElseThrow(BadCredentialsException::new);

        if (!orderEntity.getArtistId().equals(artistId)) {
            throw new ForbiddenActionException();
        }

        if (!orderEntity.getStatus().equals("PROGRESS")) {
            throw new BadActionException("You can't do this action");
        }
        orderEntity.setArtistComment(orderShortDTO.getComment());
        orderRepository.save(orderEntity);
    }

    private OrderDTO getOrderDTObyId(Integer orderId) {
        OrderEntity orderEntity = orderRepository.findById(orderId).orElseThrow(BadCredentialsException::new);

        CustomerEntity customerEntity = customerRepository.findById(orderEntity.getCustomerId()).orElseThrow(UserNotFoundException::new);
        ArtistEntity artistEntity = artistRepository.findById(orderEntity.getArtistId()).orElseThrow(UserNotFoundException::new);
        ArtEntity artEntity = artRepository.findById(orderEntity.getArtId()).orElseThrow(BadCredentialsException::new);
        Optional<CardEntity> cardOpt = cardRepository.findById(orderEntity.getCardId());
        Optional<AddressEntity> addressOpt = addressRepository.findById(orderEntity.getAddressId());
        Optional<ArtPhotoEntity> artPhotoOpt = artPhotoRepository.findByArtIdAndAndDefaultPhoto(artEntity.getId(), true);

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderId(orderId);

        if (addressOpt.isPresent()) {
            AddressEntity addressEntity = addressOpt.get();
            orderDTO.setName(addressEntity.getName());
            orderDTO.setCountry(addressEntity.getCountry());
            orderDTO.setRegion(addressEntity.getRegion());
            orderDTO.setCity(addressEntity.getCity());
            orderDTO.setIndex(addressEntity.getIndex());
            orderDTO.setLocation(addressEntity.getLocation());
        }

        if (cardOpt.isPresent()) {
            CardEntity cardEntity = cardOpt.get();
            orderDTO.setCardType(cardEntity.getType());
            orderDTO.setNumber(cardEntity.getNumber());
        }

        orderDTO.setArtistName(artistEntity.getArtistName());
        orderDTO.setCustomerName(customerEntity.getCustomerName());
        orderDTO.setArtName(artEntity.getName());
        orderDTO.setPrice(artEntity.getPrice());
        orderDTO.setStatus(orderEntity.getStatus());
        orderDTO.setArtistComment(orderEntity.getArtistComment());

        if (artPhotoOpt.isPresent()) {
            orderDTO.setArtUrl(artPhotoOpt.get().getPhotoUrl());
        } else {
            orderDTO.setArtUrl("");
        }

        return orderDTO;
    }
}