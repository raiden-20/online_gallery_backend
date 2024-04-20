package ru.vsu.cs.sheina.online_gallery_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.sheina.online_gallery_backend.dto.ArtistFullDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.ArtistRegistrationDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.ArtistShortDTO;
import ru.vsu.cs.sheina.online_gallery_backend.entity.ArtistEntity;
import ru.vsu.cs.sheina.online_gallery_backend.entity.CustomerEntity;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.UserAlreadyExistsException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.UserNotFoundException;
import ru.vsu.cs.sheina.online_gallery_backend.repository.ArtistRepository;
import ru.vsu.cs.sheina.online_gallery_backend.repository.CustomerRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final CustomerRepository customerRepository;
    private final FileService fileService;

    public ArtistFullDTO getArtistData(UUID id) {
        ArtistEntity artistEntity = artistRepository.findById(id).orElseThrow(UserNotFoundException::new);
        CustomerEntity customerEntity = customerRepository.findByArtistId(id).orElseThrow(UserNotFoundException::new);

        int views = artistEntity.getViews();
        artistEntity.setViews(++views);
        artistRepository.save(artistEntity);

        ArtistFullDTO dto = new ArtistFullDTO();

        dto.setArtistName(artistEntity.getArtistName());
        dto.setDescription(artistEntity.getDescription());
        dto.setAvatarUrl(artistEntity.getAvatarUrl());
        dto.setCoverUrl(artistEntity.getCoverUrl());
        dto.setCustomerId(customerEntity.getId());

        return dto;
    }

    public void setArtistData(String artistId, String artistName, String avatarUrl, String coverUrl, String description, MultipartFile avatar, MultipartFile cover) {
        ArtistEntity artistEntity = artistRepository.findById(UUID.fromString(artistId)).orElseThrow(UserNotFoundException::new);

        artistEntity.setArtistName(artistName);
        artistEntity.setDescription(description);

        if (!avatar.isEmpty()){
            if (!artistEntity.getAvatarUrl().isEmpty()) {
                fileService.deleteFile(artistEntity.getAvatarUrl());
            }
            String url = fileService.saveFile(avatar, artistId);
            artistEntity.setAvatarUrl(url);
        } else if (avatarUrl.equals("delete") && avatar.isEmpty()) {
            fileService.deleteFile(artistEntity.getAvatarUrl());
            artistEntity.setAvatarUrl("");
        }

        if (!cover.isEmpty()){
            if (!artistEntity.getCoverUrl().isEmpty()) {
                fileService.deleteFile(artistEntity.getCoverUrl());
            }
            String url = fileService.saveFile(cover, artistId);
            artistEntity.setCoverUrl(url);
        } else if (coverUrl.equals("delete") && cover.isEmpty()) {
            fileService.deleteFile(artistEntity.getCoverUrl());
            artistEntity.setCoverUrl("");
        }

        artistRepository.save(artistEntity);
    }

    public UUID createArtist(ArtistRegistrationDTO artistRegistrationDTO) {
        CustomerEntity customerEntity = customerRepository.findById(artistRegistrationDTO.getCustomerId()).orElseThrow(UserNotFoundException::new);
        if (customerEntity.getArtistId() != null){
            throw new UserAlreadyExistsException();
        }

        ArtistEntity artistEntity = new ArtistEntity();
        artistEntity.setId(UUID.randomUUID());
        artistEntity.setArtistName(artistRegistrationDTO.getArtistName());
        artistEntity.setDescription("");
        artistEntity.setAvatarUrl("");
        artistEntity.setCoverUrl("");
        artistEntity.setViews(0);

        artistRepository.save(artistEntity);

        customerEntity.setArtistId(artistEntity.getId());

        customerRepository.save(customerEntity);

        return artistEntity.getId();
    }

    public List<ArtistShortDTO> getArtists() {
        return artistRepository.findAll().stream()
                .map(art -> new ArtistShortDTO(art.getId(), art.getArtistName(), art.getAvatarUrl(), art.getViews()))
                .toList();
    }

    public List<ArtistShortDTO> searchArtist(String input) {
        return artistRepository.findAll().stream()
                .filter(art -> art.getArtistName().toUpperCase().contains(input.toUpperCase()))
                .map(art -> new ArtistShortDTO(art.getId(), art.getArtistName(), art.getAvatarUrl(), art.getViews()))
                .toList();
    }

    public void deleteAccount(UUID artistId) {
        ArtistEntity artistEntity = artistRepository.findById(artistId).orElseThrow(UserNotFoundException::new);
        artistRepository.delete(artistEntity);
    }
}
