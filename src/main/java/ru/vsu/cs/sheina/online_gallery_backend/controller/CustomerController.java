package ru.vsu.cs.sheina.online_gallery_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.sheina.online_gallery_backend.dto.customer.CustomerFullDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.customer.CustomerRegistrationDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.customer.CustomerShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.service.CustomerService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCustomerData(@PathVariable UUID customerId) {
        CustomerFullDTO customerFullDTO = customerService.getCustomerData(customerId);
        return ResponseEntity.ok(customerFullDTO);
    }

    @GetMapping("/customer/first-entry")
    public ResponseEntity<?> isFirstEntry(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(customerService.isFirstEntry(token));
    }

    @PutMapping(value = "/customer/data", consumes = "multipart/form-data")
    public ResponseEntity<?> setCustomerData(@RequestPart("customerName") String customerName,
                                             @RequestPart("birthDate") String birthDate,
                                             @RequestPart("gender") String gender,
                                             @RequestPart("description") String description,
                                             @RequestPart("avatarUrl") String avatarUrl,
                                             @RequestPart("coverUrl") String coverUrl,
                                             @RequestPart("avatar") MultipartFile avatar,
                                             @RequestPart("cover") MultipartFile cover,
                                             @RequestHeader("Authorization") String token) {
        customerService.setCustomerData(token, customerName, birthDate, description, gender, avatarUrl, coverUrl,
                                                avatar, cover);
        return ResponseEntity.ok("Data updated successfully");
    }

    @PostMapping("/customer/create")
    public ResponseEntity<?> createCustomer(@RequestBody CustomerRegistrationDTO customerRegistrationDTO,
                                            @RequestHeader("Authorization") String token){
        customerService.createCustomer(customerRegistrationDTO, token);
        return ResponseEntity.ok("User is registered");
    }

    @GetMapping("/customers")
    public ResponseEntity<?> getCustomers() {
        List<CustomerShortDTO> customerList = customerService.getCustomers();
        return ResponseEntity.ok(customerList);
    }

    @GetMapping("/search/customer/object={input}")
    public ResponseEntity<?> searchCustomer(@PathVariable String input) {
        List<CustomerShortDTO> customers = customerService.searchCustomer(input);
        return ResponseEntity.ok(customers);
    }
 }
