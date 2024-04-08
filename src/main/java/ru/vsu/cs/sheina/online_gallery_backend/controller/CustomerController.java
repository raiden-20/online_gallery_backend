package ru.vsu.cs.sheina.online_gallery_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.sheina.online_gallery_backend.dto.CustomerFullDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.CustomerRegistrationDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.CustomerShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.service.CustomerService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/customer/{id}")
    public ResponseEntity<?> getCustomerData(@PathVariable UUID id) {
        CustomerFullDTO customerFullDTO = customerService.getCustomerData(id);
        return ResponseEntity.ok(customerFullDTO);
    }

    @GetMapping("/customer/{id}/first-entry")
    public ResponseEntity<?> isFirstEntry(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.isFirstEntry(id));
    }

    @PutMapping(value = "/customer/data", consumes = "multipart/form-data")
    public ResponseEntity<?> setCustomerData(@RequestPart("customerId") String customerId,
                                             @RequestPart("customerName") String customerName,
                                             @RequestPart("birthDate") String birthDate,
                                             @RequestPart("gender") String gender,
                                             @RequestPart("avatarUrl") String avatarUrl,
                                             @RequestPart("coverUrl") String coverUrl,
                                             @RequestPart("avatar") MultipartFile avatar,
                                             @RequestPart("cover") MultipartFile cover) {
        customerService.setCustomerData(customerId, customerName, birthDate, gender, avatarUrl, coverUrl,
                                                avatar, cover);
        return ResponseEntity.ok("Data updated successfully");
    }

    @PostMapping("/customer/create")
    public ResponseEntity<?> createCustomer(@RequestBody CustomerRegistrationDTO customerRegistrationDTO){
        customerService.createCustomer(customerRegistrationDTO);
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
