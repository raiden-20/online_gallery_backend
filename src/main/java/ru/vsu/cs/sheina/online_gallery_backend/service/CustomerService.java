package ru.vsu.cs.sheina.online_gallery_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.sheina.online_gallery_backend.dto.customer.CustomerFullDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.customer.CustomerRegistrationDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.customer.CustomerShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.entity.CustomerEntity;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentialsException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.UserAlreadyExistsException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.UserNotFoundException;
import ru.vsu.cs.sheina.online_gallery_backend.repository.CustomerRepository;
import ru.vsu.cs.sheina.online_gallery_backend.utils.JWTParser;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final FileService fileService;
    private final ArtistService artistService;
    private final JWTParser jwtParser;

    public CustomerFullDTO getCustomerData(UUID id) {
        CustomerEntity customerEntity = customerRepository.findById(id).orElseThrow(UserNotFoundException::new);
        CustomerFullDTO dto = new CustomerFullDTO();

        dto.setCustomerName(customerEntity.getCustomerName());
        dto.setGender(customerEntity.getGender());
        dto.setDescription(customerEntity.getDescription());
        dto.setBirthDate(customerEntity.getBirthDate());
        dto.setArtistId(customerEntity.getArtistId());
        dto.setAvatarUrl(customerEntity.getAvatarUrl());
        dto.setCoverUrl(customerEntity.getCoverUrl());

        return dto;
    }

    public Boolean isFirstEntry(UUID id) {
        return !customerRepository.existsById(id);
    }

    public void setCustomerData(String token, String customerName, String birthDate, String description, String gender, String avatarUrl, String coverUrl, MultipartFile avatar, MultipartFile cover) {
        UUID userId = jwtParser.getIdFromAccessToken(token);

        CustomerEntity customerEntity = customerRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = formatter.parse(birthDate);
            Timestamp timeStampDate = new Timestamp(date.getTime());
            customerEntity.setBirthDate(timeStampDate);
        } catch (ParseException e) {
            throw new BadCredentialsException();
        }

        customerEntity.setGender(gender);
        customerEntity.setDescription(description);
        customerEntity.setCustomerName(customerName);

        if (!avatar.isEmpty()){
            if (!customerEntity.getAvatarUrl().isEmpty()) {
                fileService.deleteFile(customerEntity.getAvatarUrl());
            }
            String url = fileService.saveFile(avatar);
            customerEntity.setAvatarUrl(url);
        } else if (avatarUrl.equals("delete") && avatar.isEmpty()) {
            fileService.deleteFile(customerEntity.getAvatarUrl());
            customerEntity.setAvatarUrl("");
        }

        if (!cover.isEmpty()){
            if (!customerEntity.getCoverUrl().isEmpty()) {
                fileService.deleteFile(customerEntity.getCoverUrl());
            }
            String url = fileService.saveFile(cover);
            customerEntity.setCoverUrl(url);
        } else if (coverUrl.equals("delete") && cover.isEmpty()) {
            fileService.deleteFile(customerEntity.getCoverUrl());
            customerEntity.setCoverUrl("");
        }

        customerRepository.save(customerEntity);
    }

    public void createCustomer(CustomerRegistrationDTO customerRegistrationDTO, String token) {
        UUID userId = jwtParser.getIdFromAccessToken(token);

        if (customerRepository.existsById(userId)) {
            throw new UserAlreadyExistsException();
        }

        CustomerEntity customerEntity = new CustomerEntity();

        customerEntity.setId(userId);
        customerEntity.setCustomerName(customerRegistrationDTO.getCustomerName());
        customerEntity.setGender(customerRegistrationDTO.getGender());
        customerEntity.setDescription("");
        customerEntity.setBirthDate(customerRegistrationDTO.getBirthDate());
        customerEntity.setAvatarUrl("");
        customerEntity.setCoverUrl("");
        customerEntity.setArtistId(null);

        customerRepository.save(customerEntity);
    }

    public List<CustomerShortDTO> getCustomers() {
        return customerRepository.findAll().stream()
                .map(cust -> new CustomerShortDTO(cust.getId(), cust.getCustomerName(), cust.getAvatarUrl()))
                .toList();
    }

    public List<CustomerShortDTO> searchCustomer(String input) {
        return customerRepository.findAll().stream()
                .filter(cust -> cust.getCustomerName().toUpperCase().contains(input.toUpperCase()))
                .map(cust -> new CustomerShortDTO(cust.getId(), cust.getCustomerName(), cust.getAvatarUrl()))
                .toList();
    }

    public void deleteAccount(String token) {
        UUID userId = jwtParser.getIdFromAccessToken(token);

        CustomerEntity customerEntity = customerRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        customerRepository.delete(customerEntity);

        if (customerEntity.getArtistId() != null) {
            artistService.deleteAccount(customerEntity.getArtistId());
        }
    }
}
