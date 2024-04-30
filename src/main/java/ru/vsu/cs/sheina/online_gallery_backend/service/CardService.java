package ru.vsu.cs.sheina.online_gallery_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.sheina.online_gallery_backend.dto.card.CardDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.card.CardNewDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.entity.AddressEntity;
import ru.vsu.cs.sheina.online_gallery_backend.entity.CardEntity;
import ru.vsu.cs.sheina.online_gallery_backend.entity.CustomerPrivateSubscriptionEntity;
import ru.vsu.cs.sheina.online_gallery_backend.entity.OrderEntity;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentialsException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.ForbiddenActionException;
import ru.vsu.cs.sheina.online_gallery_backend.repository.CardRepository;
import ru.vsu.cs.sheina.online_gallery_backend.repository.CustomerPrivateSubscriptionRepository;
import ru.vsu.cs.sheina.online_gallery_backend.repository.OrderRepository;
import ru.vsu.cs.sheina.online_gallery_backend.utils.JWTParser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService {

    private final JWTParser jwtParser;
    private final CardRepository cardRepository;
    private final CustomerPrivateSubscriptionRepository customerPrivateSubscriptionRepository;
    private final OrderRepository orderRepository;

    public List<CardDTO> getCards(String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        return cardRepository.findAllByCustomerId(customerId).stream()
                .map(ent -> new CardDTO(ent.getId(), ent.getType(), ent.getNumber(), ent.getDate(), ent.getCvv(), ent.getIsDefault()))
                .toList();
    }

    public void addCard(CardNewDTO cardNewDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        CardEntity cardEntity = new CardEntity();
        cardEntity.setNumber(cardNewDTO.getNumber());
        cardEntity.setDate(cardNewDTO.getDate());
        cardEntity.setCvv(cardNewDTO.getCvv());
        cardEntity.setCustomerId(customerId);

        if (cardNewDTO.getIsDefault()) {
            Optional<CardEntity> defaultCardOpt = cardRepository.findByCustomerIdAndIsDefault(customerId, true);
            if (defaultCardOpt.isPresent()) {
                CardEntity defaultCard = defaultCardOpt.get();
                defaultCard.setIsDefault(false);
                cardRepository.save(defaultCard);
            }
            cardEntity.setIsDefault(true);
        } else {
            cardEntity.setIsDefault(!cardRepository.existsByCustomerIdAndIsDefault(customerId, true));
        }

        int first = Integer.parseInt(cardNewDTO.getNumber().substring(0, 1));

        switch (first) {
            case 4 -> cardEntity.setType("Visa");
            case 5 -> cardEntity.setType("MasterCard");
            case 6 -> cardEntity.setType("UnionPay");
            case 2 -> cardEntity.setType("Мир");
            default -> cardEntity.setType("");
        }

        cardRepository.save(cardEntity);
    }

    public void changeCard(CardDTO cardDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);

        CardEntity cardEntity = cardRepository.findById(cardDTO.getCardId()).orElseThrow(BadCredentialsException::new);

        if (!cardEntity.getCustomerId().equals(customerId)) {
            throw new ForbiddenActionException();
        }

        cardEntity.setNumber(cardDTO.getNumber());
        cardEntity.setDate(cardDTO.getDate());
        cardEntity.setCvv(cardDTO.getCvv());

        int first = Integer.parseInt(cardDTO.getNumber().substring(0, 1));

        switch (first) {
            case 4 -> cardEntity.setType("Visa");
            case 5 -> cardEntity.setType("MasterCard");
            case 6 -> cardEntity.setType("UnionPay");
            case 2 -> cardEntity.setType("Мир");
            default -> cardEntity.setType("");
        }

        if (cardDTO.getIsDefault()) {
            Optional<CardEntity> cardDefaultOpt = cardRepository.findByCustomerIdAndIsDefault(customerId, true);
            if (cardDefaultOpt.isPresent()) {
                CardEntity cardDefault = cardDefaultOpt.get();
                cardDefault.setIsDefault(false);
                cardRepository.save(cardDefault);
            }
            cardEntity.setIsDefault(true);
        } else {
            cardEntity.setIsDefault(false);
        }

        cardRepository.save(cardEntity);
    }

    public void deleteCard(IntIdRequestDTO intIdRequestDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);

        CardEntity cardEntity = cardRepository.findById(intIdRequestDTO.getId()).orElseThrow(BadCredentialsException::new);

        if (!cardEntity.getCustomerId().equals(customerId)) {
            throw new ForbiddenActionException();
        }

        customerPrivateSubscriptionRepository.deleteAllByCardId(cardEntity.getId());
        for(OrderEntity order: orderRepository.findAllByCardId(cardEntity.getId())) {
            order.setCardId(null);
            orderRepository.save(order);
        }
        cardRepository.deleteById(intIdRequestDTO.getId());

        List<CardEntity> cards = cardRepository.findAllByCustomerId(customerId);
        if (!cards.isEmpty()) {
            cards.get(0).setIsDefault(true);
            cardRepository.save(cards.get(0));
        }
    }
}
