package ru.vsu.cs.sheina.online_gallery_backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vsu.cs.sheina.online_gallery_backend.dto.address.AddressDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.address.AddressNewDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.AddressIdDTO;

@RestController
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/addresses")
    public ResponseEntity<?> getAddresses(@RequestHeader("Authorization") String token) {
        List<AddressDTO> addresses = addressService.getAddresses(token);
        return ResponseEntity.ok(addresses);
    }

    @PostMapping("/address")
    public ResponseEntity<?> addNewAddress(@RequestBody AddressNewDTO addressNewDTO) {
        addressService.addNewAddress(addressNewDTO);
        return ResponseEntity.ok("Address added successfully");
    }

    @PutMapping("/address")
    public ResponseEntity<?> changeAddress(@RequestBody AddressDTO addressDTO) {
        addressService.changeAddress(addressDTO);
        return ResponseEntity.ok("Address changed successfully");
    }

    @DeleteMapping("/address")
    public ResponseEntity<?> deleteAddress(@RequestBody AddressIdDTO addressIdDTO) {
        addressService.deleteAddress(addressIdDTO);
        return ResponseEntity.ok("Address deleted successfully");
    }
}
