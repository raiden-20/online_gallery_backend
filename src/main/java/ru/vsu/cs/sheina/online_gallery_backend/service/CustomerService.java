package ru.vsu.cs.sheina.online_gallery_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.sheina.online_gallery_backend.dto.CustomerFullDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.CustomerRegistrationDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.CustomerShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.entity.CustomerEntity;
import ru.vsu.cs.sheina.online_gallery_backend.entity.enums.Gender;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentials;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.UserAlreadyExistsException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.UserNotFoundException;
import ru.vsu.cs.sheina.online_gallery_backend.repository.CustomerRepository;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final FileService fileService;

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

    public void setCustomerData(String customerId, String customerName, String birthDate, String description, String gender, String avatarUrl, String coverUrl, MultipartFile avatar, MultipartFile cover) {
        CustomerEntity customerEntity = customerRepository.findById(UUID.fromString(customerId)).orElseThrow(UserNotFoundException::new);
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = formatter.parse(birthDate);
            Timestamp timeStampDate = new Timestamp(date.getTime());
            customerEntity.setBirthDate(timeStampDate);
        } catch (ParseException e) {
            throw new BadCredentials();
        }

        customerEntity.setGender(Gender.valueOf(gender));
        customerEntity.setDescription(description);
        customerEntity.setCustomerName(customerName);

        if (!avatar.isEmpty()){
            if (!customerEntity.getAvatarUrl().isEmpty()) {
                fileService.deleteFile(customerEntity.getAvatarUrl());
            }
            String url = fileService.saveFile(avatar, customerId);
            customerEntity.setAvatarUrl(url);
        } else if (avatarUrl.equals("delete") && avatar.isEmpty()) {
            fileService.deleteFile(customerEntity.getAvatarUrl());
            customerEntity.setAvatarUrl("");
        }

        if (!cover.isEmpty()){
            if (!customerEntity.getCoverUrl().isEmpty()) {
                fileService.deleteFile(customerEntity.getCoverUrl());
            }
            String url = fileService.saveFile(cover, customerId);
            customerEntity.setCoverUrl(url);
        } else if (coverUrl.equals("delete") && cover.isEmpty()) {
            fileService.deleteFile(customerEntity.getCoverUrl());
            customerEntity.setCoverUrl("");
        }

        customerRepository.save(customerEntity);
    }

    public void createCustomer(CustomerRegistrationDTO customerRegistrationDTO) {

        if (customerRepository.existsById(customerRegistrationDTO.getCustomerId())) {
            throw new UserAlreadyExistsException();
        }

        CustomerEntity customerEntity = new CustomerEntity();

        customerEntity.setId(customerRegistrationDTO.getCustomerId());
        customerEntity.setCustomerName(customerRegistrationDTO.getCustomerName());
        customerEntity.setGender(customerRegistrationDTO.getGender());
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
}
