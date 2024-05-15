package ru.vsu.cs.sheina.online_gallery_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.FluxSink;
import ru.vsu.cs.sheina.online_gallery_backend.dto.notification.NotificationDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.notification.NotificationType;
import ru.vsu.cs.sheina.online_gallery_backend.entity.*;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.UserNotFoundException;
import ru.vsu.cs.sheina.online_gallery_backend.repository.*;
import ru.vsu.cs.sheina.online_gallery_backend.utils.JWTParser;

import java.sql.Timestamp;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JWTParser jwtParser;
    private final NotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;
    private final ArtistRepository artistRepository;
    private final PublicSubscriptionRepository publicSubscriptionRepository;
    private final CustomerPrivateSubscriptionRepository customerPrivateSubscriptionRepository;

    Map<UUID, FluxSink<ServerSentEvent>> subscriptions = new HashMap<>();


    public void addUserToSubscriptions(UUID id, FluxSink<ServerSentEvent> fluxSink) {
        subscriptions.put(id, fluxSink);
    }

    public void deleteUserFromSubscriptions(UUID id) {
        subscriptions.remove(id);
    }

    public void sendArtReceivedNotification(OrderEntity orderEntity, CustomerEntity customerEntity) {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setType("artReceived");
        notificationEntity.setText("обновил(-а) статус заказа");
        notificationEntity.setReceiverId(orderEntity.getArtistId());
        notificationEntity.setSenderId(orderEntity.getCustomerId());
        notificationEntity.setCreateDate(new Timestamp(System.currentTimeMillis()));
        notificationEntity.setSubjectId(orderEntity.getId());

        notificationRepository.save(notificationEntity);
        String data = customerEntity.getCustomerName() + " " + notificationEntity.getText() + " №" + getOrderNum(orderEntity.getId());

        if (subscriptions.containsKey(orderEntity.getCustomerId())) {
            ServerSentEvent<Object> event = ServerSentEvent.builder()
                    .id(String.valueOf(notificationEntity.getId()))
                    .event("ORDER")
                    .comment(customerEntity.getAvatarUrl())
                    .data(data)
                    .build();
            subscriptions.get(orderEntity.getArtistId()).next(event);
        }
    }

    public void sendArtChangeCommentNotification(OrderEntity orderEntity, ArtistEntity artistEntity) {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setType("artChangeComment");
        notificationEntity.setText("обновил(-а) статус заказа");
        notificationEntity.setReceiverId(orderEntity.getCustomerId());
        notificationEntity.setSenderId(orderEntity.getArtistId());
        notificationEntity.setCreateDate(new Timestamp(System.currentTimeMillis()));
        notificationEntity.setSubjectId(orderEntity.getId());

        notificationRepository.save(notificationEntity);
        String data = artistEntity.getArtistName() + " " + notificationEntity.getText() + " №" + getOrderNum(orderEntity.getId());

        if (subscriptions.containsKey(orderEntity.getCustomerId())) {
            ServerSentEvent<Object> event = ServerSentEvent.builder()
                    .id(String.valueOf(notificationEntity.getId()))
                    .event("ORDER")
                    .comment(artistEntity.getAvatarUrl())
                    .data(data)
                    .build();
            subscriptions.get(orderEntity.getCustomerId()).next(event);
        }
    }

    public void sendArtSendNotification(OrderEntity orderEntity, ArtistEntity artistEntity) {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setType("artSend");
        notificationEntity.setText("обновил(-а) статус заказа");
        notificationEntity.setReceiverId(orderEntity.getCustomerId());
        notificationEntity.setSenderId(orderEntity.getArtistId());
        notificationEntity.setCreateDate(new Timestamp(System.currentTimeMillis()));
        notificationEntity.setSubjectId(orderEntity.getId());

        notificationRepository.save(notificationEntity);
        String data = artistEntity.getArtistName() + " " + notificationEntity.getText() + " №" + getOrderNum(orderEntity.getId());

        if (subscriptions.containsKey(orderEntity.getCustomerId())) {
            ServerSentEvent<Object> event = ServerSentEvent.builder()
                    .id(String.valueOf(notificationEntity.getId()))
                    .event("ORDER")
                    .comment(artistEntity.getAvatarUrl())
                    .data(data)
                    .build();
            subscriptions.get(orderEntity.getCustomerId()).next(event);
        }
    }
    public void sendArtSoldNotification(OrderEntity orderEntity, CustomerEntity customerEntity, ArtEntity artEntity) {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setType("artSold");
        notificationEntity.setText("купил(-а) Вашу работу:");
        notificationEntity.setReceiverId(orderEntity.getArtistId());
        notificationEntity.setSenderId(orderEntity.getCustomerId());
        notificationEntity.setCreateDate(new Timestamp(System.currentTimeMillis()));
        notificationEntity.setSubjectId(orderEntity.getId());

        notificationRepository.save(notificationEntity);
        String data = customerEntity.getCustomerName() + " " + notificationEntity.getText() + " " + artEntity.getName();

        if (subscriptions.containsKey(orderEntity.getArtistId())) {
            ServerSentEvent<Object> event = ServerSentEvent.builder()
                    .id(String.valueOf(notificationEntity.getId()))
                    .event("ORDER")
                    .comment(customerEntity.getAvatarUrl())
                    .data(data)
                    .build();
            subscriptions.get(orderEntity.getArtistId()).next(event);
        }
    }

    public void sendPrivateDeletedNotification(ArtistEntity artistEntity, PrivateSubscriptionEntity privateSubscriptionEntity) {
        List<CustomerPrivateSubscriptionEntity> subscriptionEntities = customerPrivateSubscriptionRepository.findAllByPrivateSubscriptionId(privateSubscriptionEntity.getId());

        for (CustomerPrivateSubscriptionEntity subscriptionEntity: subscriptionEntities) {
            NotificationEntity notificationEntity = new NotificationEntity();
            notificationEntity.setType("privateDeleted");
            notificationEntity.setText("отключил(-а) ежемесячную поддержку. Деньги за неиспользуемый период вернутся в течение 7 дней.");
            notificationEntity.setReceiverId(subscriptionEntity.getCustomerId());
            notificationEntity.setSenderId(artistEntity.getId());
            notificationEntity.setCreateDate(new Timestamp(System.currentTimeMillis()));
            notificationEntity.setSubjectId(null);

            notificationRepository.save(notificationEntity);
            String data = artistEntity.getArtistName() + " " + notificationEntity.getText();

            if (subscriptions.containsKey(subscriptionEntity.getCustomerId())) {
                ServerSentEvent<Object> event = ServerSentEvent.builder()
                        .id(String.valueOf(notificationEntity.getId()))
                        .event("PRIVATE_DELETED")
                        .comment(artistEntity.getAvatarUrl())
                        .data(data)
                        .build();
                subscriptions.get(subscriptionEntity.getCustomerId()).next(event);
            }
        }
    }

    public void sendNewPrivatePostNotification(PostEntity postEntity, ArtistEntity artistEntity, PrivateSubscriptionEntity privateSubscriptionEntity) {
        List<CustomerPrivateSubscriptionEntity> subscriptionEntities = customerPrivateSubscriptionRepository.findAllByPrivateSubscriptionId(privateSubscriptionEntity.getId());

        for (CustomerPrivateSubscriptionEntity subscriptionEntity: subscriptionEntities) {
            NotificationEntity notificationEntity = new NotificationEntity();
            notificationEntity.setType("newPrivatePost");
            notificationEntity.setText("выложил(-а) новый пост:");
            notificationEntity.setReceiverId(subscriptionEntity.getCustomerId());
            notificationEntity.setSenderId(artistEntity.getId());
            notificationEntity.setCreateDate(postEntity.getCreatedAt());
            notificationEntity.setSubjectId(postEntity.getId());

            notificationRepository.save(notificationEntity);
            String data = artistEntity.getArtistName() + " " + notificationEntity.getText() + " " + postEntity.getTitle();

            if (subscriptions.containsKey(subscriptionEntity.getCustomerId())) {
                ServerSentEvent<Object> event = ServerSentEvent.builder()
                        .id(String.valueOf(notificationEntity.getId()))
                        .event("POST")
                        .comment(artistEntity.getAvatarUrl())
                        .data(data)
                        .build();
                subscriptions.get(subscriptionEntity.getCustomerId()).next(event);
            }
        }
    }

    public void sendNewPublicArtNotification(ArtEntity artEntity, ArtistEntity artistEntity) {
        List<PublicSubscriptionEntity> publicSubscriptionEntities = publicSubscriptionRepository.findAllByArtistId(artistEntity.getId());
        for (PublicSubscriptionEntity subscriptionEntity: publicSubscriptionEntities) {
            NotificationEntity notificationEntity = new NotificationEntity();
            notificationEntity.setType("newPublicArt");
            notificationEntity.setText("выставил(-а) новую работу:");
            notificationEntity.setReceiverId(subscriptionEntity.getCustomerId());
            notificationEntity.setSenderId(artistEntity.getId());
            notificationEntity.setCreateDate(artEntity.getPublishDate());
            notificationEntity.setSubjectId(artEntity.getId());

            notificationRepository.save(notificationEntity);
            String data = artistEntity.getArtistName() + " " + notificationEntity.getText() + " " + artEntity.getName();

            if (subscriptions.containsKey(subscriptionEntity.getCustomerId())) {
                ServerSentEvent<Object> event = ServerSentEvent.builder()
                        .id(String.valueOf(notificationEntity.getId()))
                        .event("ART")
                        .comment(artistEntity.getAvatarUrl())
                        .data(data)
                        .build();
                subscriptions.get(subscriptionEntity.getCustomerId()).next(event);
            }
        }
    }

    public void sendNewPrivateArtNotification(ArtEntity artEntity, ArtistEntity artistEntity, PrivateSubscriptionEntity privateSubscriptionEntity) {
        List<CustomerPrivateSubscriptionEntity> subscriptionEntities = customerPrivateSubscriptionRepository.findAllByPrivateSubscriptionId(privateSubscriptionEntity.getId());

        for (CustomerPrivateSubscriptionEntity subscriptionEntity: subscriptionEntities) {
            NotificationEntity notificationEntity = new NotificationEntity();
            notificationEntity.setType("newPrivateArt");
            notificationEntity.setText("выставил(-а) новую работу:");
            notificationEntity.setReceiverId(subscriptionEntity.getCustomerId());
            notificationEntity.setSenderId(artistEntity.getId());
            notificationEntity.setCreateDate(artEntity.getPublishDate());
            notificationEntity.setSubjectId(artEntity.getId());

            notificationRepository.save(notificationEntity);
            String data = artistEntity.getArtistName() + " " + notificationEntity.getText() + " " + artEntity.getName();

            if (subscriptions.containsKey(subscriptionEntity.getCustomerId())) {
                ServerSentEvent<Object> event = ServerSentEvent.builder()
                        .id(String.valueOf(notificationEntity.getId()))
                        .event("ART")
                        .comment(artistEntity.getAvatarUrl())
                        .data(data)
                        .build();
                subscriptions.get(subscriptionEntity.getCustomerId()).next(event);
            }
        }
    }

    public List<NotificationDTO> getArtistNotification(String token) {
        UUID receiverId = jwtParser.getIdFromAccessToken(token);
        CustomerEntity customerEntity = customerRepository.findById(receiverId).orElseThrow(UserNotFoundException::new);
        UUID artistId = customerEntity.getArtistId();

        if (artistId == null) {
            throw new UserNotFoundException();
        }

        if (!artistRepository.existsById(artistId)) {
            throw new UserNotFoundException();
        }

        List<NotificationEntity> notificationEntities = notificationRepository.findAllByReceiverId(artistId);
        List<NotificationDTO> dtos = new ArrayList<>();

        for (NotificationEntity entity: notificationEntities) {
            NotificationDTO dto = new NotificationDTO();
            dto.setNotificationId(entity.getId());
            dto.setSubjectId(entity.getSubjectId());
            dto.setText(entity.getText());
            dto.setDate(entity.getCreateDate());

            changeDTOByType(dto, entity);

            dtos.add(dto);
        }
        return dtos;
    }

    public List<NotificationDTO> getCustomerNotification(String token) {
        UUID receiverId = jwtParser.getIdFromAccessToken(token);
        if (!customerRepository.existsById(receiverId)) {
            throw new UserNotFoundException();
        }

        List<NotificationEntity> notificationEntities = notificationRepository.findAllByReceiverId(receiverId);
        List<NotificationDTO> dtos = new ArrayList<>();

        for (NotificationEntity entity: notificationEntities) {
            NotificationDTO dto = new NotificationDTO();
            dto.setNotificationId(entity.getId());
            dto.setSubjectId(entity.getSubjectId());
            dto.setText(entity.getText());
            dto.setDate(entity.getCreateDate());

            changeDTOByType(dto, entity);

            dtos.add(dto);
        }
        return dtos;
    }

    private String getOrderNum(Integer id) {
        StringBuilder stringBuilder = new StringBuilder();
        int countNum = 8;
        int count = 0;
        int tmp = id;

        while (tmp > 0) {
            count++;
            tmp = tmp / 10;
        }

        while (countNum - count > 0) {
            stringBuilder.append("0");
            countNum--;
        }
        stringBuilder.append(id);

        return stringBuilder.toString();
    }

    private void changeDTOByType(NotificationDTO dto, NotificationEntity entity) {
        switch (entity.getType()) {
            case "newPublicArt" :
            case "newPrivateArt" :
                dto.setType(NotificationType.ART);
                dto.setIsSystem(false);
                Optional<ArtistEntity> artistEntity = artistRepository.findById(entity.getSenderId());
                if (artistEntity.isPresent()) {
                    dto.setAvatarUrl(artistEntity.get().getAvatarUrl());
                } else {
                    dto.setAvatarUrl("");
                }
                break;
            case "startPublicAuction" :
            case "newPublicAuction" :
            case "maximumBetBlock" :
            case "auctionWinning" :
                dto.setType(NotificationType.AUCTION);
                dto.setIsSystem(false);
                artistEntity = artistRepository.findById(entity.getSenderId());
                if (artistEntity.isPresent()) {
                    dto.setAvatarUrl(artistEntity.get().getAvatarUrl());
                } else {
                    dto.setAvatarUrl("");
                }
                break;
            case "newPrivatePost" :
                dto.setType(NotificationType.POST);
                dto.setIsSystem(false);
                artistEntity = artistRepository.findById(entity.getSenderId());
                if (artistEntity.isPresent()) {
                    dto.setAvatarUrl(artistEntity.get().getAvatarUrl());
                } else {
                    dto.setAvatarUrl("");
                }
                break;
            case "artSend" :
            case "artReceived" :
                dto.setType(NotificationType.ORDER);
                dto.setIsSystem(false);
                Optional<CustomerEntity> customerEntity = customerRepository.findById(entity.getSenderId());
                if (customerEntity.isPresent()) {
                    dto.setAvatarUrl(customerEntity.get().getAvatarUrl());
                } else {
                    dto.setAvatarUrl("");
                }
                break;
            case "artChangeComment" :
                dto.setType(NotificationType.ORDER);
                dto.setIsSystem(false);
                artistEntity = artistRepository.findById(entity.getSenderId());
                if (artistEntity.isPresent()) {
                    dto.setAvatarUrl(artistEntity.get().getAvatarUrl());
                } else {
                    dto.setAvatarUrl("");
                }
                break;
            case "privateDeleted" :
                dto.setType(NotificationType.PRIVATE_DELETED);
                dto.setIsSystem(false);
                artistEntity = artistRepository.findById(entity.getSenderId());
                if (artistEntity.isPresent()) {
                    dto.setAvatarUrl(artistEntity.get().getAvatarUrl());
                } else {
                    dto.setAvatarUrl("");
                }
                break;
            case "artBlock" :
                dto.setType(NotificationType.ART_BLOCK);
                dto.setIsSystem(true);
                dto.setAvatarUrl("");
                break;
            case "eventCreated" :
                dto.setType(NotificationType.EVENT);
                dto.setIsSystem(true);
                dto.setAvatarUrl("");
                break;
            case "artSold" :
        }
    }
}
