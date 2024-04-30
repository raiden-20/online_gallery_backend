package ru.vsu.cs.sheina.online_gallery_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.sheina.online_gallery_backend.dto.customer.CustomerFullDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.customer.CustomerRegistrationDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.customer.CustomerShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentialsException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.ForbiddenActionException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.UserAlreadyExistsException;
import ru.vsu.cs.sheina.online_gallery_backend.service.CustomerService;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Покупатель")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Получить данные покупателя")
    @ApiResponse(responseCode = "200",
            description = "Отправлены данные покупателя",
            content = @Content(schema = @Schema(implementation = CustomerFullDTO.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
    @ApiResponse(responseCode = "403",
            description = "Действие запрещено",
            content = @Content(schema = @Schema(implementation = ForbiddenActionException.class)))
    public ResponseEntity<?> getCustomerData(@PathVariable UUID customerId) {
        CustomerFullDTO customerFullDTO = customerService.getCustomerData(customerId);
        return ResponseEntity.ok(customerFullDTO);
    }

    @GetMapping("/customer/first-entry")
    @Operation(summary = "Проверка первого входа покупателя")
    @ApiResponse(responseCode = "200",
            description = "Отправлено логическое значение",
            content = @Content(schema = @Schema(implementation = Boolean.class)))
    public ResponseEntity<?> isFirstEntry(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(customerService.isFirstEntry(token));
    }


    @PutMapping(value = "/customer/data", consumes = "multipart/form-data")
    @Operation(summary = "Изменение данных покупателя")
    @ApiResponse(responseCode = "200",
            description = "Данные успешно изменены",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400",
            description = "Неверные данные",
            content = @Content(schema = @Schema(implementation = BadCredentialsException.class)))
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
    @Operation(summary = "Создание аккаунта покупателя")
    @ApiResponse(responseCode = "200",
            description = "Покупатель успешно создан",
            content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "409",
            description = "Покупатель уже существует",
            content = @Content(schema = @Schema(implementation = UserAlreadyExistsException.class)))
    public ResponseEntity<?> createCustomer(@RequestBody CustomerRegistrationDTO customerRegistrationDTO,
                                            @RequestHeader("Authorization") String token){
        customerService.createCustomer(customerRegistrationDTO, token);
        return ResponseEntity.ok("User is registered");
    }

    @GetMapping("/customers")
    @Operation(summary = "Получить всех покупателей")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список всех покупателей",
            content = @Content(schema = @Schema(implementation = CustomerShortDTO.class)))
    public ResponseEntity<?> getCustomers() {
        List<CustomerShortDTO> customerList = customerService.getCustomers();
        return ResponseEntity.ok(customerList);
    }

    @GetMapping("/search/customer/object={input}")
    @Operation(summary = "Поиск покупателей по имени")
    @ApiResponse(responseCode = "200",
            description = "Отправлен список покупателей",
            content = @Content(schema = @Schema(implementation = CustomerShortDTO.class)))
    public ResponseEntity<?> searchCustomer(@PathVariable String input) {
        List<CustomerShortDTO> customers = customerService.searchCustomer(input);
        return ResponseEntity.ok(customers);
    }
 }
