package ru.vsu.cs.sheina.online_gallery_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.order.ChangeOrderDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.order.OrderDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.order.OrderShortDTO;
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
public class OrderService {

    private final OrderRepository orderRepository;
    private final AuctionRepository auctionRepository;
    private final ArtPhotoRepository artPhotoRepository;
    private final CustomerRepository customerRepository;
    private final AuctionPhotoRepository auctionPhotoRepository;
    private final JWTParser jwtParser;
    private final ArtRepository artRepository;
    private final ArtistRepository artistRepository;
    private final CardRepository cardRepository;
    private final AddressRepository addressRepository;
    private final NotificationService notificationService;

    public Integer createAuctionOrder(Integer auctionId, UUID artistId, UUID customerId) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setCustomerId(customerId);
        orderEntity.setArtistId(artistId);
        orderEntity.setSubjectId(auctionId);
        orderEntity.setStatus("AWAIT");
        orderEntity.setType("AUCTION");
        orderEntity.setCreateDate(new Timestamp(System.currentTimeMillis()));

        orderRepository.save(orderEntity);
        return orderEntity.getId();
    }

    public void changeOrder(ChangeOrderDTO changeOrderDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);

        OrderEntity orderEntity = orderRepository.findById(changeOrderDTO.getOrderId()).orElseThrow(BadCredentialsException::new);
        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
        AuctionEntity auctionEntity = auctionRepository.findById(orderEntity.getSubjectId()).orElseThrow(BadCredentialsException::new);

        if (!orderEntity.getCustomerId().equals(customerId)) {
            throw new ForbiddenActionException();
        }

        if (!orderEntity.getStatus().equals("WAIT")) {
            throw new BadActionException("You can't change this order");
        }

        orderEntity.setStatus("CREATED");
        orderEntity.setArtistComment("");
        orderEntity.setCardId(changeOrderDTO.getCardId());
        orderEntity.setAddressId(changeOrderDTO.getAddressId());

        orderRepository.save(orderEntity);
    }

    public Integer createOrder(Integer artId, UUID customerId, Integer cardId, Integer addressId, Boolean anonymous) {
        ArtEntity artEntity = artRepository.findById(artId).orElseThrow(BadCredentialsException::new);

        OrderEntity orderEntity = new OrderEntity();

        orderEntity.setCustomerId(customerId);
        orderEntity.setSubjectId(artId);
        orderEntity.setArtistId(artEntity.getArtistId());
        orderEntity.setStatus("CREATED");
        orderEntity.setArtistComment("");
        orderEntity.setType("ART");
        orderEntity.setCardId(cardId);
        orderEntity.setAddressId(addressId);
        orderEntity.setCreateDate(new Timestamp(System.currentTimeMillis()));

        orderRepository.save(orderEntity);

        artEntity.setSold(true);
        if (anonymous) {
            artEntity.setOwnerId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        } else {
            artEntity.setOwnerId(customerId);
        }
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

        if (!orderEntity.getArtistId().equals(artistId) && !orderEntity.getCustomerId().equals(customerId)) {
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

        if (!orderEntity.getStatus().equals("PROGRESS")){
            throw new BadActionException("Bad action");
        }

        orderEntity.setStatus("FINISHED");
        orderRepository.save(orderEntity);

        CustomerEntity customerEntity = customerRepository.findById(orderEntity.getCustomerId()).orElseThrow(UserNotFoundException::new);

        notificationService.sendArtReceivedNotification(orderEntity, customerEntity);
    }

    public void send(OrderShortDTO orderShortDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        ArtistEntity artistEntity = artistRepository.findById(customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new).getArtistId()).orElseThrow(UserNotFoundException::new);

        OrderEntity orderEntity = orderRepository.findById(orderShortDTO.getId()).orElseThrow(BadCredentialsException::new);

        if (!orderEntity.getArtistId().equals(artistEntity.getId())) {
            throw new ForbiddenActionException();
        }

        orderEntity.setArtistComment(orderShortDTO.getComment());
        orderEntity.setStatus("PROGRESS");

        notificationService.sendArtSendNotification(orderEntity, artistEntity);

        orderRepository.save(orderEntity);
    }

    public void edit(OrderShortDTO orderShortDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        ArtistEntity artistEntity = artistRepository.findById(customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new).getArtistId()).orElseThrow(UserNotFoundException::new);

        OrderEntity orderEntity = orderRepository.findById(orderShortDTO.getId()).orElseThrow(BadCredentialsException::new);

        if (!orderEntity.getArtistId().equals(artistEntity.getId())) {
            throw new ForbiddenActionException();
        }

        if (!orderEntity.getStatus().equals("PROGRESS")) {
            throw new BadActionException("You can't do this action");
        }
        orderEntity.setArtistComment(orderShortDTO.getComment());
        orderRepository.save(orderEntity);
        notificationService.sendArtChangeCommentNotification(orderEntity, artistEntity);
    }

    private OrderDTO getOrderDTObyId(Integer orderId) {
        OrderEntity orderEntity = orderRepository.findById(orderId).orElseThrow(BadCredentialsException::new);

        CustomerEntity customerEntity = customerRepository.findById(orderEntity.getCustomerId()).orElseThrow(UserNotFoundException::new);
        ArtistEntity artistEntity = artistRepository.findById(orderEntity.getArtistId()).orElseThrow(UserNotFoundException::new);

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderId(orderId);
        orderDTO.setCreateDate(orderEntity.getCreateDate());

        if (orderEntity.getAddressId() != null) {
            AddressEntity addressEntity = addressRepository.findById(orderEntity.getAddressId()).orElseThrow(BadCredentialsException::new);
            orderDTO.setName(addressEntity.getName());
            orderDTO.setCountry(addressEntity.getCountry());
            orderDTO.setRegion(addressEntity.getRegion());
            orderDTO.setCity(addressEntity.getCity());
            orderDTO.setIndex(addressEntity.getIndex());
            orderDTO.setLocation(addressEntity.getLocation());
        }

        if (orderEntity.getCardId() != null) {
            CardEntity cardEntity = cardRepository.findById(orderEntity.getCardId()).orElseThrow(BadCredentialsException::new);
            orderDTO.setCardType(cardEntity.getType());
            orderDTO.setNumber(cardEntity.getNumber());
        }

        if (orderEntity.getType().equals("ART")) {
            ArtEntity artEntity = artRepository.findById(orderEntity.getSubjectId()).orElseThrow(BadCredentialsException::new);
            Optional<ArtPhotoEntity> artPhotoOpt = artPhotoRepository.findByArtIdAndAndDefaultPhoto(artEntity.getId(), true);
            orderDTO.setArtName(artEntity.getName());
            orderDTO.setPrice(artEntity.getPrice());

            if (artPhotoOpt.isPresent()) {
                orderDTO.setArtUrl(artPhotoOpt.get().getPhotoUrl());
            } else {
                orderDTO.setArtUrl("");
            }
        } else {
            AuctionEntity auctionEntity = auctionRepository.findById(orderEntity.getSubjectId()).orElseThrow(BadCredentialsException::new);
            Optional<AuctionPhotoEntity> auctionPhotoOpt = auctionPhotoRepository.findByAuctionIdAndDefaultPhoto(auctionEntity.getId(), true);
            orderDTO.setArtName(auctionEntity.getName());
            orderDTO.setPrice(auctionEntity.getCurrentPrice());

            if (auctionPhotoOpt.isPresent()) {
                orderDTO.setArtUrl(auctionPhotoOpt.get().getPhotoUrl());
            } else {
                orderDTO.setArtUrl("");
            }
        }

        orderDTO.setArtistName(artistEntity.getArtistName());
        orderDTO.setCustomerName(customerEntity.getCustomerName());

        orderDTO.setStatus(orderEntity.getStatus());
        orderDTO.setArtistComment(orderEntity.getArtistComment());

        return orderDTO;
    }
}
