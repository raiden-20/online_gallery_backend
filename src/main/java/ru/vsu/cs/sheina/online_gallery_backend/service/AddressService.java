package ru.vsu.cs.sheina.online_gallery_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.sheina.online_gallery_backend.dto.address.AddressDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.address.AddressNewDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.entity.AddressEntity;
import ru.vsu.cs.sheina.online_gallery_backend.entity.CardEntity;
import ru.vsu.cs.sheina.online_gallery_backend.entity.OrderEntity;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadActionException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentialsException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.ForbiddenActionException;
import ru.vsu.cs.sheina.online_gallery_backend.repository.AddressRepository;
import ru.vsu.cs.sheina.online_gallery_backend.repository.OrderRepository;
import ru.vsu.cs.sheina.online_gallery_backend.utils.JWTParser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final JWTParser jwtParser;

    public List<AddressDTO> getAddresses(String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        return addressRepository.findAllByCustomerId(customerId).stream()
                .map(ent -> new AddressDTO(ent.getId(), ent.getName(), ent.getCountry(), ent.getRegion(),
                        ent.getCity(), ent.getIndex(), ent.getLocation(), ent.getIsDefault()))
                .toList();
    }

    public void addNewAddress(AddressNewDTO addressNewDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setCustomerId(customerId);
        addressEntity.setName(addressNewDTO.getName());
        addressEntity.setCountry(addressNewDTO.getCountry());
        addressEntity.setRegion(addressNewDTO.getRegion());
        addressEntity.setCity(addressNewDTO.getCity());
        addressEntity.setLocation(addressNewDTO.getLocation());
        addressEntity.setIndex(addressNewDTO.getIndex());

        if (addressNewDTO.getIsDefault()) {
            Optional<AddressEntity> defaultAddressOpt = addressRepository.findByCustomerIdAndIsDefault(customerId, true);
            if (defaultAddressOpt.isPresent()) {
                AddressEntity defaultAddress = defaultAddressOpt.get();
                defaultAddress.setIsDefault(false);
                addressRepository.save(defaultAddress);
            }
            addressEntity.setIsDefault(true);
        } else {
            addressEntity.setIsDefault(!addressRepository.existsByCustomerIdAndIsDefault(customerId, true));
        }

        addressRepository.save(addressEntity);
    }

    public void changeAddress(AddressDTO addressDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);

        AddressEntity addressEntity = addressRepository.findById(addressDTO.getAddressId()).orElseThrow(BadCredentialsException::new);

        if (!addressEntity.getCustomerId().equals(customerId)) {
            throw new ForbiddenActionException();
        }

        addressEntity.setName(addressDTO.getName());
        addressEntity.setCountry(addressDTO.getCountry());
        addressEntity.setRegion(addressDTO.getRegion());
        addressEntity.setCity(addressDTO.getCity());
        addressEntity.setLocation(addressDTO.getLocation());
        addressEntity.setIndex(addressDTO.getIndex());

        if (addressDTO.getIsDefault()) {
            Optional<AddressEntity> addressDefaultOpt = addressRepository.findByCustomerIdAndIsDefault(customerId, true);
            if (addressDefaultOpt.isPresent()) {
                AddressEntity addressDefault = addressDefaultOpt.get();
                addressDefault.setIsDefault(false);
                addressRepository.save(addressDefault);
            }
            addressEntity.setIsDefault(true);
        } else {
            addressEntity.setIsDefault(false);
        }

        addressRepository.save(addressEntity);
    }

    public void deleteAddress(IntIdRequestDTO intIdRequestDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);

        AddressEntity addressEntity = addressRepository.findById(intIdRequestDTO.getId()).orElseThrow(BadCredentialsException::new);

        if (!addressEntity.getCustomerId().equals(customerId)) {
            throw new ForbiddenActionException();
        }
        for(OrderEntity order: orderRepository.findAllByAddressId(addressEntity.getId())) {
            if (!order.getStatus().equals("FINISHED")){
                throw new BadActionException("Address is used in active orders");
            }
            order.setAddressId(null);
            orderRepository.save(order);
        }
        addressRepository.deleteById(intIdRequestDTO.getId());

        List<AddressEntity> addresses = addressRepository.findAllByCustomerId(customerId);
        if (!addresses.isEmpty()) {
            addresses.get(0).setIsDefault(true);
            addressRepository.save(addresses.get(0));
        }
    }
}
