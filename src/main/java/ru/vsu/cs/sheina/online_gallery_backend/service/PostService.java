package ru.vsu.cs.sheina.online_gallery_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.sheina.online_gallery_backend.dto.post.PostChangeDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.post.PostCreateDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.post.PostDTO;
import ru.vsu.cs.sheina.online_gallery_backend.dto.field.IntIdRequestDTO;
import ru.vsu.cs.sheina.online_gallery_backend.entity.*;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.BadCredentialsException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.ForbiddenActionException;
import ru.vsu.cs.sheina.online_gallery_backend.exceptions.UserNotFoundException;
import ru.vsu.cs.sheina.online_gallery_backend.repository.*;
import ru.vsu.cs.sheina.online_gallery_backend.utils.JWTParser;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {
    
    private final PostRepository postRepository;
    private final PostPhotoRepository postPhotoRepository;
    private final CustomerRepository customerRepository;
    private final CustomerPrivateSubscriptionRepository customerPrivateSubscriptionRepository;
    private final PrivateSubscriptionRepository privateSubscriptionRepository;
    private final ArtistRepository artistRepository;
    private final NotificationRepository notificationRepository;
    private final FileService fileService;
    private final NotificationService notificationService;
    private final JWTParser jwtParser;

    public List<PostDTO> getPosts(UUID artistId, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
        PrivateSubscriptionEntity privateSubscriptionEntity = privateSubscriptionRepository.findByArtistId(artistId).orElseThrow(BadCredentialsException::new);

        if (!customerPrivateSubscriptionRepository.existsByCustomerIdAndPrivateSubscriptionId(customerId, privateSubscriptionEntity.getId())
                && !customerEntity.getArtistId().equals(artistId)) {
            throw new ForbiddenActionException();
        }

        List<PostEntity> postEntities = postRepository.findAllByArtistId(artistId);
        List<PostDTO> dtos = new ArrayList<>();

        for (PostEntity post: postEntities) {
            PostDTO dto = new PostDTO();
            dto.setPostId(post.getId());
            dto.setTitle(post.getTitle());
            dto.setText(post.getBody());
            dto.setDate(post.getCreatedAt());

            List<PostPhotoEntity> photos = postPhotoRepository.findAllByPostId(post.getId());

            List<String> urls = new ArrayList<>(photos.stream()
                    .filter(PostPhotoEntity::getDefaultPhoto)
                    .map(PostPhotoEntity::getPhotoUrl)
                    .toList());

            for (PostPhotoEntity photoEntity: photos) {
                if (!photoEntity.getDefaultPhoto()) {
                    urls.add(photoEntity.getPhotoUrl());
                }
            }

            dto.setPhotoUrls(urls);
            dtos.add(dto);
        }
        return dtos;
    }

    public void createPost(List<MultipartFile> photos, PostCreateDTO postCreateDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
        UUID artistId = customerEntity.getArtistId();

        PostEntity postEntity = new PostEntity();
        postEntity.setArtistId(artistId);
        postEntity.setTitle(postCreateDTO.getTitle());
        postEntity.setBody(postCreateDTO.getText());
        postEntity.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        postRepository.save(postEntity);

        for (int i = 0; i < photos.size(); i++) {
            PostPhotoEntity postPhotoEntity = new PostPhotoEntity();
            postPhotoEntity.setPostId(postEntity.getId());
            postPhotoEntity.setPhotoUrl(fileService.saveFile(photos.get(i), postEntity.getId().toString()));

            postPhotoEntity.setDefaultPhoto(i == 0);

            postPhotoRepository.save(postPhotoEntity);
        }

        ArtistEntity artistEntity = artistRepository.findById(artistId).orElseThrow(UserNotFoundException::new);
        PrivateSubscriptionEntity privateSubscriptionEntity = privateSubscriptionRepository.findByArtistId(artistId).orElseThrow(BadCredentialsException::new);

        notificationService.sendNewPrivatePostNotification(postEntity, artistEntity, privateSubscriptionEntity);
    }

    public void changePost(List<MultipartFile> photos, PostChangeDTO postChangeDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
        UUID artistId = customerEntity.getArtistId();

        PostEntity postEntity = postRepository.findById(postChangeDTO.getPostId()).orElseThrow(BadCredentialsException::new);

        if (!postEntity.getArtistId().equals(artistId)) {
            throw new ForbiddenActionException();
        }

        postEntity.setTitle(postChangeDTO.getTitle());
        postEntity.setBody(postChangeDTO.getText());

        for (String url: postChangeDTO.getDeletePhotoUrls()) {
            fileService.deleteFile(url);
            postPhotoRepository.deleteAllByPostIdAndAndPhotoUrl(postChangeDTO.getPostId(), url);
        }

        for (int i = 0; i < photos.size(); i++) {
            PostPhotoEntity postPhotoEntity = new PostPhotoEntity();

            postPhotoEntity.setPostId(postChangeDTO.getPostId());
            postPhotoEntity.setPhotoUrl(fileService.saveFile(photos.get(i), postEntity.getId().toString()));

            postPhotoEntity.setDefaultPhoto(i == 0);

            postPhotoRepository.save(postPhotoEntity);
        }
    }

    public void delete(IntIdRequestDTO intIdRequestDTO, String token) {
        UUID customerId = jwtParser.getIdFromAccessToken(token);
        CustomerEntity customerEntity = customerRepository.findById(customerId).orElseThrow(UserNotFoundException::new);
        UUID artistId = customerEntity.getArtistId();

        PostEntity postEntity = postRepository.findById(intIdRequestDTO.getId()).orElseThrow(BadCredentialsException::new);

        if (!postEntity.getArtistId().equals(artistId)) {
            throw new ForbiddenActionException();
        }

        notificationRepository.deleteAllBySubjectId(postEntity.getId());
        postPhotoRepository.deleteAllByPostId(postEntity.getId());
        postRepository.deleteById(postEntity.getId());
    }
}
